package br.fecap.pi.saferide;

public class Criptografia {
    // Chaves de criptografia
    private static final int APP_ENCRYPT_KEY = 7;     // App criptografa (para enviar)
    private static final int APP_DECRYPT_KEY = 5;     // App descriptografa (para receber)


    // Encriptar dados que serão enviados para o servidor
    public static String encryptForServer(String input) {
        return cifraCesar(input, APP_ENCRYPT_KEY);
    }

    // Desencriptar dados recebidos do servidor
    public static String decryptFromServer(String input) {
        return cifraCesar(input, -APP_DECRYPT_KEY);
    }

    // Metodo genérico de Cifra de César
    private static String cifraCesar(String input, int shift) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder output = new StringBuilder();
        shift = shift % 26; // Garante que o shift esteja entre 0-25

        for (char c : input.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isLowerCase(c) ? 'a' : 'A';
                c = (char) (base + (c - base + shift + 26) % 26);
            }
            output.append(c);
        }

        return output.toString();
    }
}
