package com.mathieu.paf;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class Carte extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Button positionButton = null;
    private Button autoButton = null;
    private Location position = null;
    private boolean toggle = false;
    private double lat;
    private double oldLat = 0;
    private double lon;
    private double oldLon = 0;
    private float precision = 0;
    private android.os.Handler customHandler;
    private Runnable myRunnable;
    private Button save = null;
    private Button load = null;
    private String fichierCourant = "";
    private ArrayList<PointCarte> listePointsCarte = new ArrayList<PointCarte>(); //initialisation de la liste de points GPS
    private ArrayList<Location> listePosition = new ArrayList<Location>();
    private Button nav = null;
    public int puissanceSignal ;
    private int niveauSignal ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carte);
        setUpMapIfNeeded();

        positionButton = (Button) findViewById(R.id.button);
        positionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = mMap.getMyLocation(); //NullPointer ici, étrange......
                if (position != null) {
                    lat = position.getLatitude();
                    lon = position.getLongitude();
                    precision = position.getAccuracy();
                    Toast.makeText(MainActivity.getContext(), "Position actuelle : " + lat + ", " + lon + "  Précision : " + precision, Toast.LENGTH_SHORT).show();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 16));
                } else {
                    Toast.makeText(MainActivity.getContext(), "La localisation GPS n'est pas encore disponnible.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        autoButton = (Button) findViewById(R.id.auto);
        autoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggle) {
                    autoButton.setText("Mode auto : OFF");
                    toggle = false;
                }
                else{
                    autoButton.setText("Mode auto : ON");
                    toggle = true;
                }
            }
        });

        customHandler = new android.os.Handler();
        customHandler.postDelayed(myRunnable, 0);

        myRunnable = new Runnable() {
            @Override
            public void run() {
                // Code à éxécuter de façon périodique

                if (toggle) {
                    position = mMap.getMyLocation();
                    if (position != null) { //Permet d'éviter le crash de l'appli
                        float distanceMin = 100000000; //On initialise avec une valeur importante pour etre sur que la premiere itération trouvera une valeur inférieure
                        for (Location pos : listePosition) {
                            if (position.distanceTo(pos) < distanceMin) {
                                distanceMin = position.distanceTo(pos);
                            }
                        }
                        if (position.getAccuracy() < distanceMin) {
                            lat = position.getLatitude();
                            lon = position.getLongitude();
                            listePosition.add(position); //On ajoute la dernière postion à la liste des positions relevées
                            listePointsCarte.add(new PointCarte(lat, lon, position.getAccuracy(), 1, 1));

                            // On met un marqueur en fonction de la puissance du signal
                            if (puissanceSignal < -7.5) niveauSignal = 1 ;
                            else {
                                if (puissanceSignal < -2.5) niveauSignal = 2 ;
                                else {
                                    if (puissanceSignal < 2.5) niveauSignal = 3 ;
                                    else {
                                        if (puissanceSignal < 7.5) niveauSignal = 4 ;
                                        else niveauSignal = 5 ;
                                    }
                                }
                            }
                            MarkerOptions m = new MarkerOptions().position(new LatLng(lat, lon)).anchor(0.5f,0.5f) ;
                            switch(niveauSignal) {
                                case 1 : mMap.addMarker(m.icon(BitmapDescriptorFactory.fromResource(R.drawable.marqueur_signal1)));
                                case 2 : mMap.addMarker(m.icon(BitmapDescriptorFactory.fromResource(R.drawable.marqueur_signal2)));
                                case 3 : mMap.addMarker(m.icon(BitmapDescriptorFactory.fromResource(R.drawable.marqueur_signal3)));
                                case 4 : mMap.addMarker(m.icon(BitmapDescriptorFactory.fromResource(R.drawable.marqueur_signal4)));
                                case 5 : mMap.addMarker(m.icon(BitmapDescriptorFactory.fromResource(R.drawable.marqueur_signal5)));
                            }

                            Toast.makeText(MainActivity.getContext(), "Relevé de position : " + lat + ", " + lon, Toast.LENGTH_SHORT).show();
                        }
                        else  {
                            Toast.makeText(MainActivity.getContext(), "Un marqueur est déjà présent dans le disque de précision courant.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                customHandler.postDelayed(this, 2000); //Délai entre deux relevés GPS
            }
        };
        myRunnable.run();

        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Création du dossier et du fichier : on fait une boucle sur listePoints
                File dossierPAF = new File(Environment.getExternalStorageDirectory() + "/PAF");
                if (!dossierPAF.exists()) {
                    dossierPAF.mkdirs(); //Si le dossier n'existe pas, on le crée dans la mémoire
                }
                AlertDialog.Builder popup = new AlertDialog.Builder(Carte.this);
                popup.setTitle("PAF");
                popup.setMessage("Veuillez saisir un nom pour la base de donneés :");
                final EditText input = new EditText(MainActivity.getContext());
                input.setText(fichierCourant); //On affiche le nom du fichier précédemment chargé
                input.requestFocus();
                popup.setView(input);
                popup.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().matches("")) {
                            //OK
                            File fichierSortie = new File(Environment.getExternalStorageDirectory() + "/PAF/" + input.getText().toString()); //Création de la base de données
                            try {
                                fichierSortie.createNewFile();
                                Toast.makeText(MainActivity.getContext(), "Enregistrement des données dans : " + input.getText().toString(), Toast.LENGTH_SHORT).show();
                                BufferedWriter bw = new BufferedWriter((new FileWriter(Environment.getExternalStorageDirectory() + "/PAF/" + input.getText().toString(), true))); //Permet de concaténer avec le fichier précédent.
                                for (PointCarte pointcarte : listePointsCarte) {
                                    bw.write(pointcarte.getLatitude() + ";" + pointcarte.getLongitude() + ";" + pointcarte.getPrecision() + ";" + pointcarte.getRSSI() + ";" + pointcarte.getSNR() + '\n');
                                }
                                bw.flush();
                                bw.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(MainActivity.getContext(), "Base de données enregistrée !", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
                popup.show();
            }
        });

        load = (Button) findViewById(R.id.load);
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ouverture d'un explorateur pour sélectionner un fichier
                FileDialog fd = new FileDialog(Carte.this);
                // Add a listener for capture user action
                fd.setListener(new FileDialog.ActionListener() {
                    public void userAction(int action, String filePath) {
                        // Test if user select a file
                        if (action == FileDialog.ACTION_SELECTED_FILE) {
                            Toast.makeText(MainActivity.getContext(), "Chargement de la base de données depuis : " + filePath, Toast.LENGTH_SHORT).show();
                            fichierCourant = filePath.split("/")[filePath.split("/").length - 1]; //Prend la dernière partie de l'Array

                            //Lecture de la BDD
                            try {
                                InputStream ips = new FileInputStream(filePath);
                                InputStreamReader ipsr = new InputStreamReader(ips);
                                BufferedReader br = new BufferedReader(ipsr);
                                String ligne;
                                while ((ligne = br.readLine()) != null) {
                                    System.out.println(ligne);
                                    String[] parts = ligne.split(";");
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.marqueur_signal)));
                                    //Ajouter les points à la liste listePoints
                                }
                                br.close();
                            } catch (Exception e) {
                                System.out.println(e.toString());
                            }
                            Toast.makeText(MainActivity.getContext(), "Chargement terminé !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                fd.selectFileStrict();


            }
        });


        nav = (Button) findViewById(R.id.button);
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Récupérer le layout du drawer
                //mDrawerLayout.openDrawer(Gravity.LEFT);
                //Ensuite, test sur le clic. Si 0, on ferme l'activité, si 2, on ouvre indoor
                //Pour fermer : drawer.closeDrawer(Gravity.LEFT);
            }
        });


    }

    @Override
    protected void onStop() {
        //On pourra décider d'arrêter la boucle de la Runnable ici
        customHandler.removeCallbacks(myRunnable);
        super.onStop();
    }

    @Override
    protected void onResume() { //Il faut aussi écrire onPause et onStop pour arrêter la boucle sur la runnable : juste avec le toggle ?
        super.onResume();
        setUpMapIfNeeded();
        //Et de la reprendre ici avec un .run()
        myRunnable.run();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(48.826523, 2.346354)).title("Télécom ParisTech"));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(48.826523, 2.346354)).title("Télécom ParisTech").icon(BitmapDescriptorFactory.fromResource(R.drawable.marqueur_signal)));

        mMap.setMyLocationEnabled(true);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.826523, 2.346354), 16));
    }
}
