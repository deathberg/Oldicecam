package xyz.vcxm.vmxplay.tls;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/* loaded from: classes5.dex */
public final class PkixAtDateTrust implements X509TrustManager {
    private final Set<TrustAnchor> anchors;
    private final Date when;

    public PkixAtDateTrust(long whenMillis) throws Exception {
        this.when = new Date(whenMillis);
        KeyStore ks = KeyStore.getInstance("AndroidCAStore");
        ks.load(null);
        this.anchors = new HashSet();
        Enumeration<String> e2 = ks.aliases();
        while (e2.hasMoreElements()) {
            String a2 = e2.nextElement();
            Certificate c2 = ks.getCertificate(a2);
            if (c2 instanceof X509Certificate) {
                this.anchors.add(new TrustAnchor((X509Certificate) c2, null));
            }
        }
    }

    @Override // javax.net.ssl.X509TrustManager
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (chain != null) {
            try {
                if (chain.length != 0) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    CertPath path = cf.generateCertPath(Arrays.asList(chain));
                    PKIXParameters p2 = new PKIXParameters(this.anchors);
                    p2.setRevocationEnabled(false);
                    p2.setDate(this.when);
                    CertPathValidator.getInstance("PKIX").validate(path, p2);
                    return;
                }
            } catch (GeneralSecurityException e2) {
                throw new CertificateException("pkix@date fail", e2);
            }
        }
        throw new CertificateException("empty chain");
    }

    @Override // javax.net.ssl.X509TrustManager
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override // javax.net.ssl.X509TrustManager
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public static SSLSocketFactory factory(long whenMillis) throws Exception {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{new PkixAtDateTrust(whenMillis)}, new SecureRandom());
        return ctx.getSocketFactory();
    }
}
