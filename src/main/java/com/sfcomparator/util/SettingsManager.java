package com.sfcomparator.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Gerencia persistência de configurações com criptografia AES-256-CBC.
 * A chave é derivada via PBKDF2WithHmacSHA256 combinando um segredo da
 * aplicação com o hostname da máquina, tornando o arquivo legível apenas
 * por esta aplicação na mesma máquina.
 */
public class SettingsManager {

    private static final String CIPHER_ALGORITHM   = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM       = "AES";
    private static final String KDF_ALGORITHM       = "PBKDF2WithHmacSHA256";
    private static final int    KEY_BITS            = 256;
    private static final int    KDF_ITERATIONS      = 120_000;
    private static final int    IV_BYTES            = 16;

    // Salt fixo e único desta aplicação (não é segredo, apenas diversifica a chave)
    private static final byte[] SALT = {
        (byte) 0x53, (byte) 0x46, (byte) 0x43, (byte) 0x6F,
        (byte) 0x6D, (byte) 0x70, (byte) 0x61, (byte) 0x72,
        (byte) 0x61, (byte) 0x74, (byte) 0x6F, (byte) 0x72,
        (byte) 0x32, (byte) 0x30, (byte) 0x32, (byte) 0x36
    };

    // Segredo da aplicação — combinado com o hostname vincula ao ambiente
    private static final String APP_SECRET = "SalesforceOrgComparator_v1.0@2026#SF";

    // Cabeçalho que identifica o formato do arquivo
    private static final String FILE_HEADER = "SFCS|1.0";

    // -----------------------------------------------------------------------
    // API pública
    // -----------------------------------------------------------------------

    /**
     * Criptografa {@code json} e grava no {@code file} informado.
     */
    public void saveToFile(String json, File file) throws Exception {
        SecretKey key = deriveKey();

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] iv        = cipher.getIV();
        byte[] encrypted = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));

        // Layout: [IV (16 bytes)] + [dados cifrados], tudo em Base64
        byte[] combined = new byte[IV_BYTES + encrypted.length];
        System.arraycopy(iv,        0, combined, 0,       IV_BYTES);
        System.arraycopy(encrypted, 0, combined, IV_BYTES, encrypted.length);

        String content = FILE_HEADER + "\n"
                + Base64.getEncoder().encodeToString(combined);

        Files.createDirectories(file.toPath().getParent());
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Lê e descriptografa o arquivo, retornando o JSON original.
     * Lança {@link Exception} com mensagem em português caso o arquivo seja
     * inválido, corrompido ou tiver sido criado em outra máquina.
     */
    public String loadFromFile(File file) throws Exception {
        if (!file.exists()) {
            throw new Exception("Arquivo não encontrado:\n" + file.getAbsolutePath());
        }

        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();

        // Validar cabeçalho
        String[] parts = content.split("\n", 2);
        if (parts.length < 2 || !FILE_HEADER.equals(parts[0].trim())) {
            throw new Exception(
                "Formato de arquivo inválido.\n"
                + "Certifique-se de que o arquivo foi gerado por esta aplicação.");
        }

        byte[] combined = Base64.getDecoder().decode(parts[1].trim());

        if (combined.length <= IV_BYTES) {
            throw new Exception("Arquivo de configurações corrompido (dados incompletos).");
        }

        byte[] iv        = new byte[IV_BYTES];
        byte[] encrypted = new byte[combined.length - IV_BYTES];
        System.arraycopy(combined, 0,       iv,        0, IV_BYTES);
        System.arraycopy(combined, IV_BYTES, encrypted, 0, encrypted.length);

        SecretKey key = deriveKey();
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        try {
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (BadPaddingException e) {
            throw new Exception(
                "Não foi possível descriptografar o arquivo.\n\n"
                + "Possíveis causas:\n"
                + "• O arquivo foi criado em outra máquina\n"
                + "• O arquivo está corrompido\n"
                + "• O arquivo não pertence a esta aplicação");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------------

    /**
     * Deriva a chave AES-256 a partir do segredo da aplicação + hostname.
     * O vínculo com o hostname torna o arquivo ilegível em outras máquinas.
     */
    private SecretKey deriveKey() throws Exception {
        String hostname = getHostname();
        char[] password = (APP_SECRET + "|" + hostname).toCharArray();

        SecretKeyFactory factory = SecretKeyFactory.getInstance(KDF_ALGORITHM);
        KeySpec spec = new PBEKeySpec(password, SALT, KDF_ITERATIONS, KEY_BITS);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), KEY_ALGORITHM);
    }

    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            // fallback: nome do usuário do SO
            return System.getProperty("user.name", "default-host");
        }
    }
}
