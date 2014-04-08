package de.tum.socialcomp.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainSectionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_launchpad, container, false);

        // send new game request to the server upon clicking
        rootView.findViewById(R.id.request_new_game_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    	MainActivity act = MainActivity.getInstance();
                    	if(act != null){
                    		new HttpPoster().execute(new String[]{"games", 
        							act.getFacebookID(act.getBaseContext()),
									"requestNew"});
                    	}	                     
                    }
                });


        return rootView;
    }
}

