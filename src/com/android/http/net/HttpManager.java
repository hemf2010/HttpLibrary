package com.android.http.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public class HttpManager {

	private static final String HTTPMETHOD_POST = "POST";
	private static final String HTTPMETHOD_GET = "GET";
	private static final String HTTPMETHOD_PUT = "PUT";
	private static final String HTTPMETHOD_DEL = "DELETE";

	private static final int SET_CONNECTION_TIMEOUT = 5 * 1000;
	private static final int SET_SOCKET_TIMEOUT = 20 * 1000;

	/**
	 * 
	 * @param url
	 *            服务器地址
	 * @param method
	 *            "GET","POST","PUT" or "DELETE"
	 * @param params
	 *            存放参数的容器
	 * @return 响应结果
	 * @throws HttpException
	 */
	public static String openUrl(String url, String method,
			HttpParameters params,HeaderParameters headerParams) throws HttpException {
		String result = "";
		try {
			HttpClient client = getNewHttpClient();
			HttpUriRequest request = null;
			ByteArrayOutputStream bos = null;
			
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					NetStateManager.getAPN());
			if (method.equals(HTTPMETHOD_GET)) {
				url = url + "?" + HttpUtils.encodeUrl(params);
				HttpGet get = new HttpGet(url);
				request = get;
			} else if (method.equals(HTTPMETHOD_POST)) {
				HttpPost post = new HttpPost(url);
				request = post;
				if(params.fileSize()>0){
					MultipartEntity multipartEntity = new MultipartEntity();
					//add string params
					for(int i=0;i<params.size();i++){
						multipartEntity.addPart(params.getKey(i), params.getValue(i));
					}
					// Add file params
		            int currentIndex = 0;
		            int lastIndex = params.fileSize() - 1;
					for(int j=0;j<params.fileSize();j++){
						 boolean isLast = currentIndex == lastIndex;
						 FileWrapper file = params.getFile(j);
						 if(file.inputStream != null){
							 if(file.contentType != null) {
			                        multipartEntity.addPart(params.getFileKey(j), file.getFileName(), file.inputStream, file.contentType, isLast);
			                    } else {
			                        multipartEntity.addPart(params.getFileKey(j), file.getFileName(), file.inputStream, isLast);
			                    }
						 }
						 currentIndex++;
					}
					post.setEntity(multipartEntity);
				}else{
					byte[] data = null;
					bos = new ByteArrayOutputStream();
					String postParam = HttpUtils.encodeParameters(params);
					data = postParam.getBytes("UTF-8");
					bos.write(data);
					data = bos.toByteArray();
					bos.close();
					ByteArrayEntity formEntity = new ByteArrayEntity(data);
					post.setEntity(formEntity);
				}
				
			} else if (method.equals(HTTPMETHOD_PUT)) {

			} else if (method.equals(HTTPMETHOD_DEL)) {
				request = new HttpDelete(url);
			}
			for ( int loc = 0; loc < headerParams.size(); loc++) {
				String _key=headerParams.getKey(loc);
				String _value=headerParams.getValue(_key);
                request.setHeader(_key,_value);
            }
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			int statusCode = status.getStatusCode();

			if (statusCode != 200) {
				result = readHttpResponse(response);
				throw new HttpException(result, statusCode);
			}
			result = readHttpResponse(response);
			return result;
		} catch (IOException e) {
			throw new HttpException(e);
		}
	}

	private static HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params, 10000);
			HttpConnectionParams.setSoTimeout(params, 10000);

			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			HttpConnectionParams.setConnectionTimeout(params,
					SET_CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, SET_SOCKET_TIMEOUT);
			HttpClient client = new DefaultHttpClient(ccm, params);
			return client;
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	private static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
	/**
	 * 读取HttpResponse数据
	 * 
	 * @param response
	 * @return
	 */
	private static String readHttpResponse(HttpResponse response) {
		String result = "";
		HttpEntity entity = response.getEntity();
		InputStream inputStream;
		try {
			inputStream = entity.getContent();
			ByteArrayOutputStream content = new ByteArrayOutputStream();

			Header header = response.getFirstHeader("Content-Encoding");
			if (header != null
					&& header.getValue().toLowerCase(Locale.ENGLISH).indexOf("gzip") > -1) {
				inputStream = new GZIPInputStream(inputStream);
			}
			int readBytes = 0;
			byte[] sBuffer = new byte[512];
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}
			result = new String(content.toByteArray());
			return result;
		} catch (IllegalStateException e) {
			
		} catch (IOException e) {
		}
		return result;
	}
}
