package src;

public interface Constants
{
    short STATE_NULL = 0;

    // Client State IDs:
    short STATE_CLIENT_OFFLINE = 11;
    short STATE_CLIENT_CONNECTING = 12;
    short STATE_CLIENT_ONLINE = 13;
    short STATE_CLIENT_DISCONNECTING = 14;

    // Server State IDs:
    short STATE_SERVER_CLOSED = 21;
    short STATE_SERVER_OPENING = 22;
    short STATE_SERVER_OPEN = 23;
    short STATE_SERVER_CLOSING = 24;

    // Client Network Signal IDs:
    short NET_CLIENT_PING = 101;
    short NET_CLIENT_JOIN_REQUEST = 102;
    short NET_CLIENT_CHAT = 110;

    // Server Network Signal IDs:
    short NET_SERVER_PING = 201;
    short NET_SERVER_JOIN_ACCEPT = 202;
    short NET_SERVER_JOIN_DENY_BANNED_NAME = 203;
    short NET_SERVER_JOIN_DENY_BANNED_IP = 204;
    short NET_SERVER_JOIN_DENY_NO_ROOM = 205;
    short NET_SERVER_JOIN_DENY_DUPLICATE = 206;
    short NET_SERVER_CHAT = 210;
}
