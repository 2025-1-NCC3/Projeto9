package br.fecap.pi.saferide.ApiMap;

public class IDUsuario {
    private static int userId;

    public static int getUserId() {
        return userId;
    }

    public static void setUserId(int id) {
        userId = id;
    }
}
