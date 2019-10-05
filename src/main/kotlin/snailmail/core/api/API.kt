package snailmail.core.api

class API(
        private val authAPI: AuthAPI,
        private val chatAPI: ChatAPI,
        private val messageAPI: MessageAPI,
        private val userAPI: UserAPI
) : AuthAPI by authAPI, ChatAPI by chatAPI, MessageAPI by messageAPI, UserAPI by userAPI
