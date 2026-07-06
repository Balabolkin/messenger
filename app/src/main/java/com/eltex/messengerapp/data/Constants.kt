package com.eltex.messengerapp.data

object Constants {
    // API
    const val BASE_URL = "https://study-chat.eltex-co.ru/"
    const val API_TIMEOUT = 30L

    // API paths
    const val GROUP_MEMBERS_PATH = "api/v1/groups.members"
    const val GROUP_INFO_PATH = "api/v1/groups.info"
    const val CHANNEL_MEMBERS_PATH = "api/v1/channels.members"
    const val CHANNEL_INFO_PATH = "api/v1/channels.info"

    // API parameters
    const val PARAM_ROOM_ID = "roomId"

    // Chat types
    const val CHAT_TYPE_DIRECT = "d"
    const val CHAT_TYPE_CHANNEL = "c"
    const val CHAT_TYPE_GROUP = "p"

    // Display user statuses
    const val STATUS_ADMIN = "Админ"
    const val STATUS_OWNER = "Владелец"

    // Error messages
    const val DEFAULT_ERROR = "Неизвестная ошибка"
    const val UNSUPPORTED_ROOM_TYPE = "Unsupported room type"

    // Pagination
    const val PAGE_SIZE = 20

    // Roles (for server)
    const val ROLE_ADMIN = "admin"

    // Default values
    const val DEFAULT_GROUP_NAME = "Группа"
}