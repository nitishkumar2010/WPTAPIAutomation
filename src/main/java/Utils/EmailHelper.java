package Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.sun.mail.util.MailConnectException;

public class EmailHelper {

	private String EmailId;
	private String Host;
	int maximumRetries = 3;
	private String Password;
	private Properties properties;
	private Session session;
	private Store store;
	private Config testConfig;

	private int attachmentSize = 0;
	private ArrayList<String> attachmentFileNamesList = new ArrayList<String>();
	private Object msgContent = null;

	/**
	 * For setting email configuration
	 * 
	 * @param EmailConfigDetails
	 */
	public EmailHelper(Config testConfig) {
		this.testConfig = testConfig;
		EmailId = "ex2apptesting1234@gmail.com";
		Password = "ex2app@12345";
		Host = "imap.gmail.com";
		properties = System.getProperties();
		properties.setProperty("mail.store.protocol", "imaps");
		properties.setProperty("mail.smtp.socketFactory.port", "993");
		properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.port", "993");
		initiateSession();
	}

	public List<Element> getLinksFromMessageContent(Message message) {
		String emailContent = getMessageContent(message);
		Document doc = Jsoup.parse(emailContent);
		return doc.select("a");
	}

	/**
	 * @param message
	 * @return Content from message
	 */
	public String getMessageContent(Message message) {
		try {
			Object msgContent = message.getContent();
			String content = "";
			if (msgContent instanceof Multipart) {
				Multipart multipart = (Multipart) msgContent;
				for (int j = 0; j < multipart.getCount(); j++) {
					javax.mail.BodyPart bodyPart = multipart.getBodyPart(j);
					content = content + bodyPart.getContent().toString();
				}
				return content;
			} else {
				content = message.getContent().toString();
				return content;
			}
		} catch (IOException | MessagingException e) {
			testConfig.logException(e);
			return null;
		}
	}

	/**
	 * Gets unread messages by search term from inbox
	 * 
	 * @param SearchPattern
	 *            - pattern in subject of mail
	 * @param waitRetryPeriod
	 *            - maximum polling period
	 * @return list of matching messages
	 * @throws IOException
	 */
	public ArrayList<Message> getMessagesBySearchTerm(String SearchPattern, int waitRetryPeriod) throws IOException {
		int waitInterval = waitRetryPeriod / maximumRetries;
		int tryCount = 0;
		while (tryCount++ < maximumRetries) {
			try {
				testConfig.logComment("Searching messages by term " + SearchPattern);
				ArrayList<Message> FilteredMessages = new ArrayList<>();
				List<Message> messages = getUnreadMessages();

				for (Message message : messages) {
					if (message.getContent().toString().contains(SearchPattern))
						FilteredMessages.add(message);
				}
				if (FilteredMessages.size() > 0) {
					return FilteredMessages;
				} else if (FilteredMessages.size() == 0) {
					Browser.wait(testConfig, waitInterval);
				}
			} catch (MessagingException e) {
				testConfig.logException(e);
				return null;
			}
		}
		testConfig.logFail("Failed to find message by term " + SearchPattern);
		return null;
	}

	/**
	 * Gets unread messages by search term from inbox
	 * 
	 * @param SearchPattern
	 *            - pattern in content of mail
	 * @param waitRetryPeriod
	 *            - maximum polling period
	 * @return list of matching messages
	 */
	public ArrayList<Message> getMessagesBySearchTermInContent(String SearchPattern, int waitRetryPeriod) {

		int waitInterval = waitRetryPeriod / maximumRetries;
		int tryCount = 0;
		while (tryCount++ < maximumRetries) {
			try {
				testConfig.logComment("Searching messages by term " + SearchPattern + " in content");
				ArrayList<Message> FilteredMessages = new ArrayList<>();
				List<Message> messages = getUnreadMessages();
				String MessageContent;
				for (Message message : messages) {
					try {
						Multipart multipart = (Multipart) message.getContent();
						MessageContent = multipart.getBodyPart(1).getContent().toString();
					} catch (ClassCastException e) {
						MessageContent = (String) message.getContent();
					}
					testConfig.logComment(MessageContent);
					if (MessageContent.contains(SearchPattern))
						FilteredMessages.add(message);
				}
				if (FilteredMessages.size() > 0) {
					return FilteredMessages;
				} else if (FilteredMessages.size() == 0) {
					Browser.wait(testConfig, waitInterval);
				}
			} catch (MessagingException | IOException e) {
				testConfig.logException(e);
				return null;
			}
		}
		testConfig.logFail("Failed to find message by term " + SearchPattern);
		return null;
	}

	/**
	 * Gets unread messages by search term from inbox
	 * 
	 * @param SearchPattern
	 *            - pattern in subject of mail
	 * @param waitRetryPeriod
	 *            - maximum polling period
	 * @return list of matching messages
	 * @throws IOException
	 */
	public ArrayList<Message> getMessagesBySearchTermInSubject(String SearchPattern, int waitRetryPeriod) {
		int waitInterval = waitRetryPeriod / maximumRetries;
		int tryCount = 0;
		while (tryCount++ < maximumRetries) {
			try {
				testConfig.logComment("Searching messages by term " + SearchPattern);
				ArrayList<Message> FilteredMessages = new ArrayList<>();
				List<Message> messages = getUnreadMessages();

				for (Message message : messages) {
					if (message.getSubject().toString().contains(SearchPattern))
						FilteredMessages.add(message);
				}
				if (FilteredMessages.size() > 0) {
					return FilteredMessages;
				} else if (FilteredMessages.size() == 0) {
					Browser.wait(testConfig, waitInterval);
				}
			} catch (MessagingException e) {
				testConfig.logException(e);
				return null;
			}
		}
		testConfig.logFail("Failed to find message by term " + SearchPattern);
		return null;
	}

	/**
	 * This Method is used to Search Email based on Subject and return the
	 * Message. If Message is not found even after 3 retries then return null
	 * 
	 * @param SearchPattern
	 * @param waitRetryPeriod
	 * @return
	 */
	public ArrayList<Message> getMessagesBySearchTermInSubjectAndReturnNullIfNoMailReceived(String SearchPattern,
			int waitRetryPeriod) {
		int waitInterval = waitRetryPeriod / maximumRetries;
		int tryCount = 0;
		while (tryCount++ < maximumRetries) {
			try {
				testConfig.logComment("Searching messages by term " + SearchPattern);
				ArrayList<Message> FilteredMessages = new ArrayList<>();
				List<Message> messages = getUnreadMessages();

				for (Message message : messages) {
					if (message.getSubject().toString().contains(SearchPattern))
						FilteredMessages.add(message);
				}
				if (FilteredMessages.size() > 0) {
					return FilteredMessages;
				} else if (FilteredMessages.size() == 0) {
					Browser.wait(testConfig, waitInterval);
				}
			} catch (MessagingException e) {
				testConfig.logException(e);
				return null;
			}
		}
		testConfig.logComment("Failed to find message by term " + SearchPattern);
		return null;
	}

	/**
	 * Gets list of messages by search term from inbox
	 * 
	 * @param SearchPattern
	 *            - pattern in subject of mail
	 * @return list of matching messages
	 * @throws IOException
	 */
	public ArrayList<Message> getListOfMessagesBySearchSubject(String SearchPattern) {

		try {
			testConfig.logComment("Searching messages by term " + SearchPattern);
			ArrayList<Message> FilteredMessages = new ArrayList<>();
			List<Message> messages = getUnreadMessages();
			for (Message message : messages) {
				if (message.getSubject().toString().contains(SearchPattern))
					FilteredMessages.add(message);
			}
			return FilteredMessages;
		} catch (MessagingException e) {
			testConfig.logException(e);
			return null;
		}
	}

	/**
	 * Gets unread messages from the inbox Recent 10 emails are returned in case
	 * of more than 10
	 * 
	 * @return list of messages
	 */
	public List<Message> getUnreadMessages() {
		try {
			Folder inbox = store.getFolder("INBOX");
			// Set as read write so that messages accessed gets set to read
			inbox.open(Folder.READ_WRITE);
			Flags seen = new Flags(Flags.Flag.SEEN);
			FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
			Message[] messages = inbox.search(unseenFlagTerm);
			List<Message> messagesList = Arrays.asList(messages);

			if (messagesList.size() > 10) {
				return messagesList.subList(messagesList.size() - 10, messagesList.size());
			} else
				return messagesList;

		} catch (MessagingException e) {
			testConfig.logException(e);
			return null;
		}
	}

	/**
	 * Initiates a session with specified information For email
	 */
	public void initiateSession() {
		int retries = 3;
		int countOfTries = 0;
		while (countOfTries < retries) {
			try {
				testConfig.logComment("Initiating session for email id " + EmailId);
				session = Session.getDefaultInstance(properties, null);
				store = session.getStore("imaps");
				store.connect(Host, EmailId, Password);
				testConfig.logComment("Session iniated successfully");
				countOfTries = retries;
			} catch (MailConnectException exception) {
				if (countOfTries == retries) {
					testConfig.logException(exception);
				} else {
					testConfig.logComment("Could not connect with mail. Trying again.");
					countOfTries++;
				}
			} catch (MessagingException e) {
				testConfig.logException(e);
				countOfTries++;
			}
		}
	}

	/**
	 * Marks all the unread messages as read.
	 */
	public void markAllMessagesAsRead() {

		List<Message> allUnreadMessages = getUnreadMessages();
		for (int i = 0; i < allUnreadMessages.size(); i++) {
			try {
				allUnreadMessages.get(i).setFlag(Flags.Flag.SEEN, true);
			} catch (MessagingException e) {
				testConfig.logException(e);
			}
		}
	}

	/**
	 * Gets messages by search term from inbox
	 * 
	 * @param SearchPattern
	 *            - pattern in subject of mail Delete messages for filtered
	 *            messages by subject
	 * @throws IOException
	 */
	public void DeleteMessagesForSearchTermInSubject(String SearchPattern) {
		try {
			Folder folderInbox = store.getFolder("INBOX");
			folderInbox.open(Folder.READ_WRITE);
			// fetches new messages from server
			Message[] Messages = folderInbox.getMessages();
			for (int i = 0; i < Messages.length; i++) {
				Message message = Messages[i];
				String subject = message.getSubject();
				if (subject.contains(SearchPattern)) {
					message.setFlag(Flags.Flag.DELETED, true);
					System.out.println("Marked DELETE for message: " + subject);
				}
			}
		} catch (MessagingException e) {
			testConfig.logException(e);
		}
	}

	/**
	 * To set other properties other than mail store protocol
	 * 
	 * @param PropertyMap
	 *            - Details of properties to be set in key, value pair
	 */
	public void setProperties(HashMap<String, String> PropertyMap) {

		for (String PropertyName : PropertyMap.keySet()) {
			properties.setProperty(PropertyName, PropertyMap.get(PropertyName));
		}
	}

	/**
	 * This Method is used to Parse Email and Save Attachments at provided
	 * location
	 * 
	 * @param email
	 * @param fileDownloadPath
	 */
	public void parseEmailAndSaveAttachments(ArrayList<Message> email, String fileDownloadPath) {
		try {
			ArrayList<String> attachments = new ArrayList<>();
			// Download attachments in claim mail
			msgContent = email.get(0).getContent();
			if (msgContent instanceof Multipart) {
				Multipart multipart = (Multipart) msgContent;
				for (int j = 0; j < multipart.getCount(); j++) {
					MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(j);
					BodyPart bodyPart = multipart.getBodyPart(j);
					String disposition = bodyPart.getDisposition();
					if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) {
						DataHandler handler = bodyPart.getDataHandler();
						attachmentFileNamesList.add(handler.getName());
						// testConfig.logComment("Attachment file name : " +
						// attachmentFileNamesList.get(j));
						attachments.add(handler.getName());
						++attachmentSize;
						if (fileDownloadPath == null)
							fileDownloadPath = testConfig.downloadPath;
						part.saveFile(fileDownloadPath + File.separator + part.getFileName());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to fetch no. of Attachments in the E-Mail
	 * 
	 * @return
	 */
	public int getAttachmentSize() {
		return attachmentSize;
	}

	/**
	 * This method is used to fetch Name of files present as attachment in Email
	 * 
	 * @return
	 */
	public ArrayList<String> getAttachedFileName() {
		return attachmentFileNamesList;
	}

	/**
	 * Function to fetch text message from Email
	 * 
	 * @param message
	 * @return
	 */
	public String getTextFromMessage(Message message) {
		String result = "";
		try {
			if (message.isMimeType("text/plain")) {
				result = message.getContent().toString();
			} else if (message.isMimeType("multipart/*")) {
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
				result = getTextFromMimeMultipart(mimeMultipart);
			}
		} catch (MessagingException | IOException e) {
			testConfig.logFail("Error in fetching text content from email: " + e);
		}
		return result;
	}

	/**
	 * Function to get text message from mime multipart
	 * 
	 * @param mimeMultipart
	 * @return
	 */
	private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) {
		String result = "";
		try {
			int count = mimeMultipart.getCount();
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.isMimeType("text/plain")) {
					result = result + "\n" + bodyPart.getContent();
					break; // without break same text appears twice in my tests
				} else if (bodyPart.isMimeType("text/html")) {
					String html = (String) bodyPart.getContent();
					result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
				} else if (bodyPart.getContent() instanceof MimeMultipart) {
					result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
				}
			}
		} catch (MessagingException | IOException e) {
			testConfig.logFail("Error in fetching text content from email: " + e);
		}
		return result;
	}

	/**
	 * Function to close IMAP account
	 */
	public void closeConnection() {

		try {
			// store.getFolder("INBOX").close(true);
			store.close();
		} catch (MessagingException e) {
			testConfig.logFail("Error in closing email account " + e);
			e.printStackTrace();
		}
	}
}
