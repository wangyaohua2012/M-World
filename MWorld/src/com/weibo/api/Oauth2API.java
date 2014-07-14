package com.weibo.api;

import java.util.Date;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.sina.weibo.sdk.utils.Utility;
import com.weibo.constants.OauthConstants;
import com.weibo.entities.AccessToken;

/**
 * 
 * @author MengMeng
 * 
 */
public class Oauth2API implements OauthConstants {

	/** 微博授权服务的地址 */
	private static final String API_SERVER = "https://api.weibo.com/oauth2";
	/** POST 请求方式 */
	protected static final String HTTPMETHOD_POST = "POST";
	/** GET 请求方式 */
	protected static final String HTTPMETHOD_GET = "GET";
	/** HTTP 参数 */
	protected static final String KEY_ACCESS_TOKEN = "access_token";

	/**
	 * 获取用户验证时需要引导用户跳转的url,通过用户验证后授权Token
	 * 
	 * @return
	 */
	public static String fetchAuthorizeUrl() {
		return API_SERVER + "/authorize" + "?client_id=" + APP_KEY
				+ "&redirect_uri=" + REDIRECT_URL
				+ "&response_type=code&display=mobile";

	}

	/**
	 * 获取授权过的Access Token
	 * 
	 * @param code
	 * @param listener
	 *            异步请求回调接口，onComplete方法获取token
	 */
	public static void accessToken(String code, AjaxCallBack<String> callBack) {
		AjaxParams params = new AjaxParams();
		params.put("client_id", APP_KEY);
		params.put("client_secret", APP_SECRET);
		params.put("grant_type", GRANT_TYPE);
		params.put("code", code);
		params.put("redirect_uri", REDIRECT_URL);
		new FinalHttp().post(API_SERVER + "/access_token", params, callBack);
	}

	/**
	 * 查询用户access_token的授权相关信息
	 * 
	 * @param token 
	 * @param callBack
	 *            异步请求回调接口，onComplete方法获取token信息
	 */
	public static void getTokenInfo(AccessToken token,
			AjaxCallBack<String> callBack) {
		AjaxParams params = new AjaxParams();
		params.put(KEY_ACCESS_TOKEN, token.access_token);
		new FinalHttp().post(API_SERVER + "/get_token_info", params, callBack);
	}

	/**
	 * 授权回收接口，帮助开发者主动取消用户的授权
	 * <p>
	 * 应用场景
	 * </p>
	 * <p>
	 * 1.应用下线时，清空所有用户的授权
	 * </p>
	 * <p>
	 * 2.应用新上线了功能，需要取得用户scope权限，可以回收后重新引导用户授权
	 * </p>
	 * <p>
	 * 3.开发者调试应用，需要反复调试授权功能
	 * </p>
	 * <p>
	 * 4.应用内实现类似登出微博帐号的功能
	 * </p>
	 * 
	 * @param token
	 * @param callBack
	 *            异步请求回调接口 ，onComplete方法获取取消授权的结果,格式为：{"result":"true" }
	 */
	public static void revokeOauth2(AccessToken token,
			AjaxCallBack<String> callBack) {
		AjaxParams params = new AjaxParams();
		params.put(KEY_ACCESS_TOKEN, token.access_token);
		new FinalHttp().post(API_SERVER + "/revokeOauth2", params, callBack);
	}

	/**
	 * 通过用户名密码验证
	 * 
	 * @param context
	 * @param username
	 * @param password
	 * @param callBack
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public static void authorize(final Context context, final String username,
			final String password, final AjaxCallBack<String> callBack) {
		WebView mWebView = new WebView(context);

		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);

		mWebView.setWebViewClient(new WeiboWebViewClient(username, password,
				callBack));
		WebChromeClient webChromeClient = new WebChromeClient() {

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				if ("oauth_failed".equals(message)) {
					Toast.makeText(context, "用户名/密码错误", Toast.LENGTH_SHORT)
							.show();
				}
				result.confirm();
				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, JsResult result) {
				Log.i("WebChromeClient", "onJsConfirm:" + message + " url:"
						+ url);
				return super.onJsConfirm(view, url, message, result);
			}
		};
		mWebView.setWebChromeClient(webChromeClient);

		mWebView.loadUrl(Oauth2API.fetchAuthorizeUrl() + "&timestamp="
				+ new Date().getTime());
		Log.i("method:authorize", "Load Url:" + Oauth2API.fetchAuthorizeUrl());
	}

	/**
	 * 验证页面所需要的WebViewClient 请用AjaxCallBack回调接口来构造对象，以获得AccessToken
	 * 
	 * @author MengMeng
	 * 
	 */
	public static class WeiboWebViewClient extends WebViewClient {
		boolean oauthFlag = false;
		boolean reload = false;
		private String username, password;
		private AjaxCallBack<String> mCallBack;

		public WeiboWebViewClient(String username, String password,
				AjaxCallBack<String> callBack) {
			super();
			this.username = username;
			this.password = password;
			mCallBack = callBack;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			String reUrl = view.getUrl();
			if (!oauthFlag) {
				if (!TextUtils.isEmpty(username)
						&& !TextUtils.isEmpty(password)) {
					view.loadUrl("javascript:(function() {"
							+ "   try{document.getElementById('userId').value='"
							+ username
							+ "';"
							+ "   document.getElementById('passwd').value='"
							+ password
							+ "';"
							+ "   document.getElementsByName('authZForm')[0].submit();"
							+ "   }catch(err){}" + "        })();");
				}
				oauthFlag = true;
			}
			if (reUrl.equals(API_SERVER + "/authorize") && !reload) {
				view.loadUrl("javascript:(function() {"
						+ "   var node=document.getElementsByName('uid')[0];"
						+ "   if(undefined==node){alert('oauth_failed'); return;}"
						+ "   var uid=document.getElementsByName('uid')[0].value;"
						+ "   if(uid==''){"
						+ "       alert('oauth_failed');"
						+ "   }else {"
						+ "        document.getElementsByName('authZForm')[0].submit();"
						+ "   }" + "        })();");
				reload = true;
				return;
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			processUrl(view, url);
		}

		private void processUrl(WebView view, String url) {
			if (url.startsWith(REDIRECT_URL)) {
				Bundle values = Utility.parseUrl(url);
				String code = values.getString("code");
				if (null == code || TextUtils.isEmpty(code)) {
					Context context = view.getContext();
					Toast.makeText(context, "未能获取code", Toast.LENGTH_SHORT)
							.show();
					view.destroy();

				} else {
					accessToken(code, mCallBack);
				}
			}
		}
	}

}