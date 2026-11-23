package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Cuidados com a senha: ela NUNCA é guardada no banco como foi digitada.
 * Guardamos um "hash" (uma impressão digital irreversível, gerada com SHA-256)
 * misturado com um "salt" (um texto aleatório próprio de cada usuário).
 * Para conferir o login, calcula-se o hash da senha digitada e compara-se com o do banco.
 */
public final class Senha {

    private Senha() {
    }

    /** Gera um salt aleatório (32 caracteres). */
    public static String gerarSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return paraHex(bytes);
    }

    /** Calcula o hash SHA-256 de (salt + senha), em hexadecimal (64 caracteres). */
    public static String gerarHash(String senha, String salt) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] resumo = sha256.digest((salt + senha).getBytes(StandardCharsets.UTF_8));
            return paraHex(resumo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }

    /** Confere se a senha digitada gera o mesmo hash que está guardado no banco. */
    public static boolean confere(String senhaDigitada, String salt, String hashGuardado) {
        return gerarHash(senhaDigitada, salt).equals(hashGuardado);
    }

    private static String paraHex(byte[] bytes) {
        StringBuilder texto = new StringBuilder();
        for (byte b : bytes) {
            texto.append(String.format("%02x", b));
        }
        return texto.toString();
    }
}
