package de.tum.socialcomp.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class GameSectionFragment extends Fragment {
	 private WebView webView;
	 
	 @Override
	 public void onActivityCreated(Bundle savedInstanceState) {
		 // reload the website 
		 super.onActivityCreated(savedInstanceState);
		 if (webView != null) {
			 webView.loadUrl(Configuration.ServerURL);
		 }
	 }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
           Bundle savedInstanceState) {
   	 if (webView != null) {
            webView.destroy();
        }
        webView = new WebView(getActivity());
        webView.loadUrl(Configuration.ServerURL);
        return webView;
         
   }
}