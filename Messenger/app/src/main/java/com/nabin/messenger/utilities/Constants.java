package com.nabin.messenger.utilities;

import java.util.HashMap;

public class Constants {

    // Vars
    public static final String TAG = "MyTag";

    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_PREFERENCE_NAME = "messengerAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_IMAGE = "image";
    public static final String STARTER_ACTIVITY = "activityName";
    public static final String USER_DATA = "userData";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";

    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "dateTime";

    public static final String KEY_COLLECTION_RECENT_CONVERSATION = "RecentConversation";
    public static final String KEY_SENDER_NAME = "SenderName";
    public static final String KEY_RECEIVER_NAME = "ReceiverName";
    public static final String KEY_LAST_MESSAGE = "LastMessage";
    public static final String KEY_RECEIVER_IMAGE = "ReceiverImage";
    public static final String KEY_SENDER_IMAGE = "SenderImage";

    public static final String KEY_USER_AVAILABILITY = "availability";

    public static final String REMOTE_MESSAGE_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MESSAGE_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MESSAGE_DATA = "data";
    public static final String REMOTE_MESSAGE_REGISTRATION_IDS = "registration_ids";

    public static final String STORY_POST_MEDIA = "storyMedia";
    public static final String STORY_POST_DIRECTORY = "stories";
    public static final String ALL_STORY_POST_DIRECTORY = "allStories";
    public static final String STORY_POST_TIME = "postTime";
    public static final String STORY_END_TIME = "endTime";
    public static final String STORY_CAPTION = "story_caption";
    public static final String STORY_MEDIA_URL = "storyMediaUrl";


    public static HashMap<String, String> remoteMessageHeaders = null;

    public static HashMap<String, String> getRemoteMessageHeaders() {
        if (remoteMessageHeaders == null) {
            remoteMessageHeaders = new HashMap<>();
            remoteMessageHeaders.put(REMOTE_MESSAGE_AUTHORIZATION,
                    "key=AAAAbI7BxvM:APA91bFvpJ-EPpADmxl_V86RNNBTnGhjKhQ2yit4gnbSmB8KV0s8NWFiabz67pNTAj1C5VkYCwOGarH2rCE7kvUD1WyCsGKP9JLRkHkwwRqvB9nhgQOEnetBroWU3nzzZ1kTKsY61Gbt");

            remoteMessageHeaders.put(REMOTE_MESSAGE_CONTENT_TYPE,
                    "application/json");
        }
        return remoteMessageHeaders;
    }

//    BCP-dfN3hePC17eB1d4E4u5BjRQNM41Jp98wG67JFkRc99X-ZEL6HpdB4lU2Q7MnVE_DzVDaoZd5Xq7V86pZtvo

    //Arrays
    public static final String[] TITLES = {"CHATS", "STATUS"};
}
