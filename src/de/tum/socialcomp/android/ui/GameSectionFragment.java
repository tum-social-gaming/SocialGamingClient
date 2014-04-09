package de.tum.socialcomp.android.ui;

import de.tum.socialcomp.android.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


/**
 * This Fragment is used to display the game statistics
 * from the webservice. Instead of using yet another
 * JSON based request we simply show a website
 * (the index site) hosted by our webservice in an 
 * embedded WebView (Browser).
 * 
 * @author Niklas Klügel
 *
 */
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