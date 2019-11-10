package snailmail.core.api

import snailmail.core.*
import java.util.*

// TODO: make endpoints more accurate!
// TODO: write more docs!

/**
 * API specification
 *
 * BE AWARE: Every method can throw:
 * @throws ProtocolErrorException
 * @throws InternalServerErrorException
 */
interface Api {
    // Authentication

    /**
     * Creates a user registration entry
     * @return AuthToken on success
     * @throws UnavailableUsernameException if username is already taken by someone else
     */
    @Endpoint("/users/register", "POST")
    fun register(credentials: UserCredentials): AuthToken

    /**
     * Authenticates an already registered user
     * @return AuthToken
     * @throws WrongCredentialsException if there is no user with such credentials (or the password is wrong)
     */
    @Endpoint("/users/authenticate", "POST")
    fun authenticate(credentials: UserCredentials): AuthToken

    /**
     * Username must be the same
     */
    fun changeCredentials(authToken: AuthToken, credentials: UserCredentials): AuthToken

    // Chats.Common

    /**
     * Retrieves a list of __all__ chats that are available to the user
     * @return List of chats
     * @throws InvalidTokenException if token is wrong, expired, etc.
     */
    @Endpoint("/chats", "GET")
    fun getChats(token: AuthToken): List<Chat>

    /**
     * Retrieves a list of __all__ chats that are available to user
     * @return Chat on success
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistException if there is no such chat (or it is unavailable to the user)
     */
    @Endpoint("/chats/id/{chat}", "GET")
    fun getChatById(token: AuthToken, chat: UUID): Chat

    // Chats.Personal

    /**
     * Retrieves a PersonalChat with the specified user (or generates one if it doesn't exist)
     * There might be only one personal chat for every pair of users
     * User is allowed to make a personal chat with himself (saved messages analogue)
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws UserDoesNotExistException if specified user id is wrong
     */
    @Endpoint("/chats/personal/{user}", "GET")
    fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat

    // Chats.Group

    /**
     * Creates a new group chat with specified title and members
     * The user (creator) is considered to be the owner of the chat
     * @param invitedMembers: should not contain the owner, all users must be distinct
     * @param title: should not be empty
     * @return created GroupChat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ProtocolErrorException with description if the contract on params is not fulfilled
     */
    @Endpoint("/chats/group", "POST")
    fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat

    fun inviteUserToGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat
    fun kickUserFromGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat
    fun removeUserFromBlacklistOfGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat
    fun leaveGroupChat(token: AuthToken, chat: UUID): GroupChat

    fun changeTitleOfGroupChat(token: AuthToken, chat: UUID, title: String): GroupChat
    fun updateAvatarOfGroupChat(token: AuthToken, chat: UUID, photo: Photo): GroupChat

    fun updatePublicTagOfGroupChat(token: AuthToken, chat: UUID, publicTag: String): GroupChat
    fun withdrawPublicTagOfGroupChat(token: AuthToken, chat: UUID): GroupChat

    fun makeNewPrivateInviteToken(token: AuthToken, chat: UUID): GroupChat
    fun withdrawPrivateInvitationalToken(token: AuthToken, chat: UUID): GroupChat

    fun setPreferredTitleOfGroupChat(token: AuthToken, chat: UUID, title: String): GroupChatPreferences
    fun getGroupChatPreferences(token: AuthToken, chat: UUID): GroupChatPreferences

    // Media

    fun prepareUpload(token: AuthToken, mediaType: String): UUID
    fun getMedia(token: AuthToken, media: UUID): Media

    // Messages

    @Endpoint("/chats/{chat}/messages", "GET")
    fun getChatMessages(token: AuthToken, chat: UUID): List<Message>

    fun deleteMessage(token: AuthToken, message: UUID): DeletedMessage

    @Endpoint("/chats/{chat}/messages", "POST")
    fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage

    fun editTextMessage(token: AuthToken, message: UUID, newText: String): TextMessage

    fun sendMediaMessage(token: AuthToken, media: UUID, caption: String): MediaMessage
    fun editMediaMessageCaption(token: AuthToken, message: UUID, newCaption: String): MediaMessage

    // Users

    @Endpoint("/users/username/{username}", "GET")
    fun getUserByUsername(token: AuthToken, username: String): User

    @Endpoint("/users/id/{id}", "GET")
    fun getUserById(token: AuthToken, user: UUID): User

    // Contacts

    fun getContact(token: AuthToken, user: UUID): Contact
    fun updateContactDisplayName(token: AuthToken, user: UUID, displayName: String): Contact
    fun banContact(token: AuthToken, user: UUID): Contact
    fun unbanContact(token: AuthToken, user: UUID): Contact

    // Profile management

    fun updateProfileDisplayName(token: AuthToken, displayName: String): User
    fun updateProfileEmail(token: AuthToken, email: String?): User
    fun updateProfileAvatar(token: AuthToken, avatar: Photo?): User

    // Searching

    fun searchAmongMessages(token: AuthToken, text: String): List<Message>
    fun searchAmongUsers(token: AuthToken, text: String): List<User>
    fun searchAmongChats(token: AuthToken, text: String): List<Chat>
}
