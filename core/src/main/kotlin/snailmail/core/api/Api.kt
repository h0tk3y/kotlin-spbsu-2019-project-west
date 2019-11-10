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
     * Changes the credentials of the user
     * Username must be the same
     * @return new AuthToken
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ProtocolErrorException if usernames does not match
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
     * @throws ChatDoesNotExistOrUnavailableException if there is no such chat (or it is unavailable to the user)
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

    /**
     * Joins a group chat using specified public tag
     * @return GroupChat on success
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException (also in case current user is in the blacklist)
     */
    fun joinGroupChatUsingPublicTag(token: AuthToken, tag: String): GroupChat

    /**
     * Joins a group chat using specified public tag
     * @return GroupChat on success
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException (also in case current user is in the blacklist)
     */
    fun joinGroupChatUsingInviteToken(token: AuthToken, inviteToken: String): GroupChat

    /**
     * Invites a user to the specified group chat
     * Automatically removes user from blacklist if necessary
     * @return updated GroupChat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     * @throws UserIsBannedException if the target user blocked causer or vice versa
     * @throws UserIsAlreadyMemberException
     */
    fun inviteUserToGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat

    /**
     * Removes the target user from chat members and puts it into the blacklist
     *
     * Owner cannot be kicked out
     *
     * @return updated GroupChat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     * @throws UserIsNotMemberException if the target user is not a member of this chat
     * @throws ProtocolErrorException if the target user is the current user (use leaveGroupChat)
     * @throws OperationFailedException if the target user is the owner
     */
    fun kickUserFromGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat

    /**
     * Removes the specified user from blacklist
     * @return updated GroupChat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     * @throws ProtocolErrorException if the target user is not in the blacklist
     */
    fun removeUserFromBlacklistOfGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat

    /**
     * Leave the specified group chat
     *
     * What happens when owner leaves the chat:
     * (*) if there is no more users is the group chat, it must be deleted
     * (*) otherwise the new owner is the first user in the members list
     *
     * @return updated GroupChat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     */
    fun leaveGroupChat(token: AuthToken, chat: UUID): GroupChat

    /**
     * Changes title of the specified chat
     * @return updated GroupChat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     */
    fun changeTitleOfGroupChat(token: AuthToken, chat: UUID, title: String): GroupChat

    /**
     * Updates the avatar of the specified chat
     * @return updated GroupChat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     * @throws MediaDoesNotExistException
     */
    fun updateAvatarOfGroupChat(token: AuthToken, chat: UUID, photo: Photo): GroupChat

    /**
     * Updates the public tag of the specified chat
     * @return updated GroupChat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     * @throws PublicTagIsUnavailableException
     */
    fun updatePublicTagOfGroupChat(token: AuthToken, chat: UUID, publicTag: String): GroupChat

    /**
     * Removes the public tag of the specified chat
     * @return updated GroupChat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     * @throws ProtocolErrorException if chat doesn't have a public tag
     */
    fun withdrawPublicTagOfGroupChat(token: AuthToken, chat: UUID): GroupChat

    /**
     * Server generates new private invitational token for the specified chat
     * @return updated GroupChat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     */
    fun makeNewPrivateInviteToken(token: AuthToken, chat: UUID): GroupChat

    /**
     * Withdraws invitational token of the specified chat
     * @return updated GroupChat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     * @throws ProtocolErrorException if chat doesn't have an invitational token
     */
    fun withdrawPrivateInviteToken(token: AuthToken, chat: UUID): GroupChat

    /**
     * Sets preferred chat's title for current user
     * @return updated GroupChatPreferences
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     */
    fun setPreferredTitleOfGroupChat(token: AuthToken, chat: UUID, title: String): GroupChatPreferences

    /**
     * @return GroupChatPreferences for specified chat
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     */
    fun getGroupChatPreferences(token: AuthToken, chat: UUID): GroupChatPreferences

    // Media

    /**
     * Asks server to generate a UUID for a new media instance
     * That UUID then used to upload a media itself
     * TODO: process of media upload is a subject to discuss
     * @return new Media's UUID
     * @throws InvalidTokenException if token is wrong, expired, etc.
     */
    fun prepareUpload(token: AuthToken, mediaType: String): UUID

    /**
     * Retrieves Media metainfo using specified UUID
     * @return Media
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws MediaDoesNotExistException
     */
    fun getMedia(token: AuthToken, media: UUID): Media

    // Messages

    /**
     * Retrieves __all__ messages related to the specified chat in chronological order
     * @return List of Messages
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException
     */
    @Endpoint("/chats/{chat}/messages", "GET")
    fun getChatMessages(token: AuthToken, chat: UUID): List<Message>

    /**
     * Deletes specified message
     * @return DeletedMessage instance to replace the original one
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException if the chat, message is related to, is not available anymore (e.g. user left it)
     * @throws MessageDoesNotExistException
     */
    fun deleteMessage(token: AuthToken, message: UUID): DeletedMessage

    /**
     * Sends a text message to the specified chat
     * @return Sent text message
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException (or in case the target user banned us or vice versa)
     * @throws ProtocolErrorException if text is empty
     */
    @Endpoint("/chats/{chat}/messages", "POST")
    fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage

    /**
     * Replaces the content of the specified text message with a new one
     * @return Updated text message
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException (or in case the target user banned us or vice versa)
     * @throws ProtocolErrorException if text is empty
     * @throws OperationFailedException if time period since message has been sent is greater than 2 days // TODO: discuss
     */
    fun editTextMessage(token: AuthToken, message: UUID, newText: String): TextMessage

    /**
     * Sends a media message to the specified chat
     * @return Sent media message
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException (or in case the target user banned us or vice versa)
     * @throws MediaDoesNotExistException\
     */
    fun sendMediaMessage(token: AuthToken, media: UUID, caption: String): MediaMessage

    /**
     * Replaces the caption of the specified media message with a new one
     * @return Updated media message
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws ChatDoesNotExistOrUnavailableException (or in case the target user banned us or vice versa)
     * @throws OperationFailedException if time period since message has been sent is greater than 2 days // TODO: discuss
     */
    fun editMediaMessageCaption(token: AuthToken, message: UUID, newCaption: String): MediaMessage

    // Users

    /**
     * Retrieves a user using username
     * @return User
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws UserDoesNotExistException
     */
    @Endpoint("/users/username/{username}", "GET")
    fun getUserByUsername(token: AuthToken, username: String): User

    /**
     * Retrieves a user using id
     * @return User
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws UserDoesNotExistException
     */
    @Endpoint("/users/id/{id}", "GET")
    fun getUserById(token: AuthToken, user: UUID): User

    // Contacts

    /**
     * Retrieves a contact metainfo for the specified user
     * @return Contact
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws UserDoesNotExistException
     */
    fun getContact(token: AuthToken, user: UUID): Contact

    /**
     * Updates preferred display name for the specified user
     * @return updated Contact
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws UserDoesNotExistException
     */
    fun updateContactDisplayName(token: AuthToken, user: UUID, displayName: String): Contact

    /**
     * Bans specified user
     * @return updated Contact
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws UserDoesNotExistException
     */
    fun banContact(token: AuthToken, user: UUID): Contact

    /**
     * Unbans specified user
     * @return updated Contact
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws UserDoesNotExistException
     */
    fun unbanContact(token: AuthToken, user: UUID): Contact

    /**
     * Checks whether the specified user banned us or not
     * @return True if he banned us, False otherwise
     * @throws InvalidTokenException if token is wrong, expired, etc.
     * @throws UserDoesNotExistException
     */
    fun checkBannedFor(token: AuthToken, user: UUID): Boolean

    // Profile management

    /**
     * Updates the current user public display name
     * @return updated User
     * @throws InvalidTokenException if token is wrong, expired, etc.
     */
    fun updateProfileDisplayName(token: AuthToken, displayName: String): User

    /**
     * Updates the current user public email (or removes one)
     * @return updated User
     * @throws InvalidTokenException if token is wrong, expired, etc.
     */
    fun updateProfileEmail(token: AuthToken, email: String?): User

    /**
     * Updates the current user public avatar (or removes one)
     * @return updated User
     * @throws InvalidTokenException if token is wrong, expired, etc.
     */
    fun updateProfileAvatar(token: AuthToken, avatar: Photo?): User

    // Searching

    /**
     * @return List of Messages that are related to the specified text
     * @throws InvalidTokenException if token is wrong, expired, etc.
     */
    fun searchAmongMessages(token: AuthToken, text: String): List<Message>

    /**
     * @return List of Users that are related to the specified text
     * @throws InvalidTokenException if token is wrong, expired, etc.
     */
    fun searchAmongUsers(token: AuthToken, text: String): List<User>

    /**
     * @return List of Chats that are related to the specified text
     * @throws InvalidTokenException if token is wrong, expired, etc.
     */
    fun searchAmongChats(token: AuthToken, text: String): List<Chat>
}
