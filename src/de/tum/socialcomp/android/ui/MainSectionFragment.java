package de.tum.socialcomp.android.ui;

import de.tum.socialcomp.android.MainActivity;
import de.tum.socialcomp.android.R;
import de.tum.socialcomp.android.R.id;
import de.tum.socialcomp.android.R.layout;
import de.tum.socialcomp.android.webservices.util.HttpPoster;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This Fragment is used to start the game, it simply
 * shows one button that triggers a request at the 
 * webservice to start new game.
 *  
 * @author Niklas Klügel
 *
 */
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

