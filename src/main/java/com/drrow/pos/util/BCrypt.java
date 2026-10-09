package com.drrow.pos.util;

import java.security.SecureRandom;

/**
 * BCrypt password hashing implementation.
 * Provides standard OpenBSD-compatible BCrypt password hashing.
 */
public class BCrypt {
    private static final int BCRYPT_SALT_LEN = 16;
    private static final int BLOWFISH_NUM_ROUNDS = 16;

    private static final int P_ORIG[] = {
        0x243f6a88, 0x85a308d3, 0x13198a2e, 0x03707344, 0xa4093822,
        0x299f31d0, 0x082efa98, 0xec4e6c89, 0x452821e6, 0x38d01377,
        0xbe5466cf, 0x34e90c6c, 0xc0ac29b7, 0xc97c50dd, 0x3f84d5b5,
        0xb5470917, 0x9216d5d9, 0x8979fb1b
    };

    private static final int S_ORIG[] = {
        0xd1310ba6, 0x98dfb5ac, 0x2ffd72db, 0xd01adfb7, 0xb8e1afed,
        0x6a267e96, 0xba7c9045, 0xf12c7f99, 0x24a19947, 0xb3916cf7,
        0x0801f2e2, 0x858efc16, 0x636920d8, 0x71574e69, 0xa458fea3,
        0xf4933d7e, 0x0d95748f, 0x728eb658, 0x718bcd58, 0x82154aee,
        0x7b54a41d, 0xc25a59b5, 0x9c30d539, 0x2af26013, 0xc5d1b023,
        0x286085f0, 0xca417918, 0xb8db38ef, 0x8e79dcb0, 0x603a180e,
        0x6c9e0e8b, 0xb01e8a3e, 0xd71577c1, 0xbd314b27, 0x78af2fda,
        0x55605c60, 0xe65525f3, 0xaa55ab94, 0x57489862, 0x63e81440,
        0x55ca396a, 0x2aab10b6, 0xb4cc5c34, 0x1141e8ce, 0xa15486af,
        0x7c72e993, 0xb3ee1411, 0x636fbc2a, 0x2ba9c55d, 0x741831f6,
        0xce5c3e16, 0x9b87931e, 0xafd6ba33, 0x6c24cf5c, 0x7a325381,
        0x28958677, 0x3b8f4898, 0x6b4bb9af, 0xc4bfe81b, 0x66282193,
        0x61d809cc, 0xfb21a991, 0x487cac60, 0x5dec8032, 0xef845d5d,
        0xe98575b1, 0xdc262302, 0xeb651b88, 0x23893e81, 0xd396acc5,
        0x0f6d6ff3, 0x83f44239, 0x2e0b4482, 0xa4842004, 0x69c8f04a,
        0x9e1f9b5e, 0x21c66842, 0xf6e96c9a, 0x670c9c61, 0xabd388f0,
        0x6a51a0d2, 0xd8542f68, 0x960fa728, 0xab5133a3, 0x6eef0b6c,
        0x137a3be4, 0xba3bf050, 0x7efb2bbe, 0x9b114277, 0x413f2f05,
        0x85547f25, 0x920a4fc5, 0xdd4d0230, 0xfe49f478, 0x2f739c40,
        0xcc607f6d, 0xb0337076, 0x17914107, 0xfe5ff079, 0x07f75f63,
        0x6ff34026, 0x80c1fe6b, 0x1866db06, 0x497f7934, 0x6424bcb3,
        0x8061b97b, 0x2f6c44a0, 0xe072d4d3, 0x0e4e2040, 0x10dbb80a,
        0x0a649996, 0xfa32e8f2, 0x42a97bca, 0x410ff205, 0x1b770775,
        0x7331ce62, 0xd0cb7a09, 0x832c3b9e, 0xb49f78e6, 0x302816d2,
        0xcc637f58, 0x7093472d, 0xad16d0f1, 0xa7986641, 0xb33a0054,
        0x1a910281, 0x05b22ffc, 0x80718824, 0x2c3f8cc5, 0x27ee45ec,
        0xfa80e227, 0x4e05b46e, 0xc3ac3746, 0x926befd4, 0xf20a4783,
        0x3826e966, 0x4ac4033e, 0x474006b4, 0x801a3745, 0x301f7a29,
        0x2c002242, 0xaa0f143a, 0x5102da0f, 0x40a04cb4, 0xa72432d5,
        0x0baf4a02, 0xac4228ce, 0xa002204c, 0xaf7a048c, 0x60a19008
    };

    private static final int BCRYPT_WORDS = 6;
    private static final int bf_crypt_ciphertext[] = {
        0x4f727068, 0x65616e42, 0x65686f6c,
        0x64657253, 0x63727970, 0x7442696e
    };

    private static final char base64_code[] = {
        '.', '/', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
        'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9'
    };

    private static final byte index_64[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1,
        54, 55, 56, 57, 58, 59, 60, 61, 62, 63, -1, -1, -1, -1, -1, -1,
        -1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, -1, -1, -1, -1, -1,
        -1, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, -1, -1, -1, -1, -1
    };

    private int P[];
    private int S[];

    private static void encode_base64(byte d[], int len, StringBuilder rs) {
        int off = 0;
        int c1, c2;
        while (off < len) {
            c1 = d[off++] & 0xff;
            rs.append(base64_code[(c1 >> 2) & 0x3f]);
            c1 = (c1 & 0x03) << 4;
            if (off >= len) {
                rs.append(base64_code[c1 & 0x3f]);
                break;
            }
            c2 = d[off++] & 0xff;
            c1 |= (c2 >> 4) & 0x0f;
            rs.append(base64_code[c1 & 0x3f]);
            c1 = (c2 & 0x0f) << 2;
            if (off >= len) {
                rs.append(base64_code[c1 & 0x3f]);
                break;
            }
            c2 = d[off++] & 0xff;
            c1 |= (c2 >> 6) & 0x03;
            rs.append(base64_code[c1 & 0x3f]);
            rs.append(base64_code[c2 & 0x3f]);
        }
    }

    private static byte char64(char x) {
        if ((int) x < 0 || (int) x >= index_64.length) return -1;
        return index_64[(int) x];
    }

    private static byte[] decode_base64(String s, int maxolen) {
        StringBuilder rs = new StringBuilder();
        int off = 0, slen = s.length(), olen = 0;
        byte ret[] = new byte[maxolen];
        byte c1, c2, c3, c4, o;

        while (off < slen - 1 && olen < maxolen) {
            c1 = char64(s.charAt(off++));
            c2 = char64(s.charAt(off++));
            if (c1 == -1 || c2 == -1) break;
            o = (byte) ((c1 << 2) | ((c2 & 0x30) >> 4));
            ret[olen++] = o;
            if (off >= slen || olen >= maxolen) break;
            c3 = char64(s.charAt(off++));
            if (c3 == -1) break;
            o = (byte) (((c2 & 0x0f) << 4) | ((c3 & 0x3c) >> 2));
            ret[olen++] = o;
            if (off >= slen || olen >= maxolen) break;
            c4 = char64(s.charAt(off++));
            o = (byte) (((c3 & 0x03) << 6) | c4);
            ret[olen++] = o;
        }
        return ret;
    }

    private final void encipher(int lr[], int off) {
        int l = lr[off];
        int r = lr[off + 1];

        l ^= P[0];
        r ^= (((S[(l >>> 24) & 0xff] + S[0x100 | ((l >>> 16) & 0xff)]) ^ S[0x200 | ((l >>> 8) & 0xff)]) + S[0x300 | (l & 0xff)]) ^ P[1];
        l ^= (((S[(r >>> 24) & 0xff] + S[0x100 | ((r >>> 16) & 0xff)]) ^ S[0x200 | ((r >>> 8) & 0xff)]) + S[0x300 | (r & 0xff)]) ^ P[2];
        r ^= (((S[(l >>> 24) & 0xff] + S[0x100 | ((l >>> 16) & 0xff)]) ^ S[0x200 | ((l >>> 8) & 0xff)]) + S[0x300 | (l & 0xff)]) ^ P[3];
        l ^= (((S[(r >>> 24) & 0xff] + S[0x100 | ((r >>> 16) & 0xff)]) ^ S[0x200 | ((r >>> 8) & 0xff)]) + S[0x300 | (r & 0xff)]) ^ P[4];
        r ^= (((S[(l >>> 24) & 0xff] + S[0x100 | ((l >>> 16) & 0xff)]) ^ S[0x200 | ((l >>> 8) & 0xff)]) + S[0x300 | (l & 0xff)]) ^ P[5];
        l ^= (((S[(r >>> 24) & 0xff] + S[0x100 | ((r >>> 16) & 0xff)]) ^ S[0x200 | ((r >>> 8) & 0xff)]) + S[0x300 | (l & 0xff)]) ^ P[6];
        r ^= (((S[(l >>> 24) & 0xff] + S[0x100 | ((r >>> 16) & 0xff)]) ^ S[0x200 | ((r >>> 8) & 0xff)]) + S[0x300 | (r & 0xff)]) ^ P[7];
        l ^= (((S[(r >>> 24) & 0xff] + S[0x100 | ((r >>> 16) & 0xff)]) ^ S[0x200 | ((r >>> 8) & 0xff)]) + S[0x300 | (r & 0xff)]) ^ P[8];
        r ^= (((S[(l >>> 24) & 0xff] + S[0x100 | ((l >>> 16) & 0xff)]) ^ S[0x200 | ((l >>> 8) & 0xff)]) + S[0x300 | (l & 0xff)]) ^ P[9];
        l ^= (((S[(r >>> 24) & 0xff] + S[0x100 | ((r >>> 16) & 0xff)]) ^ S[0x200 | ((r >>> 8) & 0xff)]) + S[0x300 | (l & 0xff)]) ^ P[10];
        r ^= (((S[(l >>> 24) & 0xff] + S[0x100 | ((l >>> 16) & 0xff)]) ^ S[0x200 | ((l >>> 8) & 0xff)]) + S[0x300 | (r & 0xff)]) ^ P[11];
        l ^= (((S[(r >>> 24) & 0xff] + S[0x100 | ((r >>> 16) & 0xff)]) ^ S[0x200 | ((r >>> 8) & 0xff)]) + S[0x300 | (r & 0xff)]) ^ P[12];
        r ^= (((S[(l >>> 24) & 0xff] + S[0x100 | ((l >>> 16) & 0xff)]) ^ S[0x200 | ((l >>> 8) & 0xff)]) + S[0x300 | (r & 0xff)]) ^ P[13];
        l ^= (((S[(r >>> 24) & 0xff] + S[0x100 | ((r >>> 16) & 0xff)]) ^ S[0x200 | ((r >>> 8) & 0xff)]) + S[0x300 | (l & 0xff)]) ^ P[14];
        r ^= (((S[(l >>> 24) & 0xff] + S[0x100 | ((l >>> 16) & 0xff)]) ^ S[0x200 | ((r >>> 8) & 0xff)]) + S[0x300 | (l & 0xff)]) ^ P[15];
        l ^= (((S[(r >>> 24) & 0xff] + S[0x100 | ((r >>> 16) & 0xff)]) ^ S[0x200 | ((r >>> 8) & 0xff)]) + S[0x300 | (r & 0xff)]) ^ P[16];

        lr[off] = r ^ P[17];
        lr[off + 1] = l;
    }

    private static int streamtoword(byte data[], int offp[]) {
        int i, word = 0, off = offp[0];
        for (i = 0; i < 4; i++) {
            word = (word << 8) | (data[off] & 0xff);
            off = (off + 1) % data.length;
        }
        offp[0] = off;
        return word;
    }

    private void init_key() {
        P = (int[]) P_ORIG.clone();
        S = (int[]) S_ORIG.clone();
    }

    private void ekskey(byte data[], byte key[]) {
        int i, offp[] = { 0 };
        int lr[] = { 0, 0 };

        for (i = 0; i < P.length; i++) {
            P[i] = P[i] ^ streamtoword(key, offp);
        }

        offp[0] = 0;
        for (i = 0; i < P.length; i += 2) {
            lr[0] ^= streamtoword(data, offp);
            lr[1] ^= streamtoword(data, offp);
            encipher(lr, 0);
            P[i] = lr[0];
            P[i + 1] = lr[1];
        }

        for (i = 0; i < S.length; i += 2) {
            lr[0] ^= streamtoword(data, offp);
            lr[1] ^= streamtoword(data, offp);
            encipher(lr, 0);
            S[i] = lr[0];
            S[i + 1] = lr[1];
        }
    }

    private byte[] crypt_raw(byte password[], byte salt[], int log_rounds) {
        int rounds = 1 << log_rounds;
        byte cdata[] = (byte[]) salt.clone();

        init_key();
        ekskey(salt, password);

        for (int i = 0; i < rounds; i++) {
            key(password);
            key(salt);
        }

        int ctext[] = (int[]) bf_crypt_ciphertext.clone();
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < (BCRYPT_WORDS * 4); j += 8) {
                encipher(ctext, j / 4);
            }
        }

        byte ret[] = new byte[BCRYPT_WORDS * 4];
        for (int i = 0, j = 0; i < BCRYPT_WORDS; i++) {
            ret[j++] = (byte) ((ctext[i] >> 24) & 0xff);
            ret[j++] = (byte) ((ctext[i] >> 16) & 0xff);
            ret[j++] = (byte) ((ctext[i] >> 8) & 0xff);
            ret[j++] = (byte) (ctext[i] & 0xff);
        }
        return ret;
    }

    private void key(byte key[]) {
        int i, offp[] = { 0 }, lr[] = { 0, 0 };
        for (i = 0; i < P.length; i++) {
            P[i] = P[i] ^ streamtoword(key, offp);
        }
        for (i = 0; i < P.length; i += 2) {
            encipher(lr, 0);
            P[i] = lr[0];
            P[i + 1] = lr[1];
        }
        for (i = 0; i < S.length; i += 2) {
            encipher(lr, 0);
            S[i] = lr[0];
            S[i + 1] = lr[1];
        }
    }

    public static String gensalt(int log_rounds) {
        SecureRandom random = new SecureRandom();
        byte rnd[] = new byte[BCRYPT_SALT_LEN];
        random.nextBytes(rnd);
        StringBuilder rs = new StringBuilder();
        rs.append("$2a$");
        if (log_rounds < 10) rs.append("0");
        rs.append(log_rounds);
        rs.append("$");
        encode_base64(rnd, rnd.length, rs);
        return rs.toString();
    }

    public static String gensalt() {
        return gensalt(10);
    }

    public static String hashpw(String password, String salt) {
        BCrypt B = new BCrypt();
        String real_salt;
        byte pass[], saltb[], salt_bytes[];
        int log_rounds;

        if (salt == null || salt.length() < 28) {
            throw new IllegalArgumentException("Invalid salt");
        }
        if (salt.charAt(0) != '$' || salt.charAt(1) != '2' || (salt.charAt(2) != 'a' && salt.charAt(2) != 'y' && salt.charAt(2) != 'b') || salt.charAt(3) != '$') {
            throw new IllegalArgumentException("Invalid salt version");
        }

        int minor = salt.charAt(2);
        log_rounds = Integer.parseInt(salt.substring(4, 6));
        real_salt = salt.substring(7, 29);
        salt_bytes = decode_base64(real_salt, BCRYPT_SALT_LEN);

        try {
            pass = password.getBytes("UTF-8");
        } catch (Exception e) {
            pass = password.getBytes();
        }

        byte passwordb[] = new byte[pass.length + 1];
        System.arraycopy(pass, 0, passwordb, 0, pass.length);
        passwordb[pass.length] = 0;

        byte hashed[] = B.crypt_raw(passwordb, salt_bytes, log_rounds);

        StringBuilder res = new StringBuilder();
        res.append("$2a$");
        if (log_rounds < 10) res.append("0");
        res.append(log_rounds);
        res.append("$");
        encode_base64(salt_bytes, salt_bytes.length, res);
        encode_base64(hashed, BCRYPT_WORDS * 4 - 1, res);
        return res.toString();
    }

    public static boolean checkpw(String plaintext, String hashed) {
        if (plaintext == null || hashed == null) return false;
        try {
            String newHash = hashpw(plaintext, hashed);
            return slowEquals(hashed, newHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean slowEquals(String a, String b) {
        int diff = a.length() ^ b.length();
        for (int i = 0; i < a.length() && i < b.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
