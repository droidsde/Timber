package com.naman14.timber.fragments;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.naman14.timber.R;
import com.naman14.timber.adapters.AlbumSongsAdapter;
import com.naman14.timber.dataloaders.AlbumLoader;
import com.naman14.timber.dataloaders.AlbumSongLoader;
import com.naman14.timber.lastfmapi.LastFmClient;
import com.naman14.timber.lastfmapi.callbacks.ArtistInfoListener;
import com.naman14.timber.lastfmapi.models.ArtistQuery;
import com.naman14.timber.lastfmapi.models.LastfmArtist;
import com.naman14.timber.models.Album;
import com.naman14.timber.models.Song;
import com.naman14.timber.utils.Constants;
import com.naman14.timber.utils.FabAnimationUtils;
import com.naman14.timber.utils.TimberUtils;
import com.naman14.timber.widgets.DividerItemDecoration;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

/**
 * Created by naman on 22/07/15.
 */
public class AlbumDetailFragment extends Fragment  {

    long albumID=-1;

    ImageView albumArt,artistArt;
    TextView albumTitle,albumDetails;

    RecyclerView recyclerView;
    AlbumSongsAdapter mAdapter;

    Toolbar toolbar;
    CoordinatorLayout coordinatorLayout;

    Album album;

    CollapsingToolbarLayout collapsingToolbarLayout;
    AppBarLayout appBarLayout;
    FloatingActionButton fab;

    public static AlbumDetailFragment newInstance(long id) {
        AlbumDetailFragment fragment = new AlbumDetailFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ALBUM_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            albumID = getArguments().getLong(Constants.ALBUM_ID);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(
                R.layout.fragment_album_detail, container, false);

        getActivity().postponeEnterTransition();

        albumArt=(ImageView) rootView.findViewById(R.id.album_art);
        artistArt=(ImageView) rootView.findViewById(R.id.artist_art);
        albumTitle=(TextView) rootView.findViewById(R.id.album_title);
        albumDetails=(TextView) rootView.findViewById(R.id.album_details);

        toolbar=(Toolbar) rootView.findViewById(R.id.toolbar);

        fab=(FloatingActionButton) rootView.findViewById(R.id.fab);

        recyclerView=(RecyclerView) rootView.findViewById(R.id.recyclerview);
        collapsingToolbarLayout=(CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        appBarLayout=(AppBarLayout) rootView.findViewById(R.id.app_bar);

        getActivity().getWindow().getEnterTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                FabAnimationUtils.scaleOut(fab, 50, null);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
//                initActivityTransitions();
                setupToolbar();
                setAlbumDetails();
                setUpAlbumSongs();
                FabAnimationUtils.scaleIn(fab);
                Drawable drawable = MaterialDrawableBuilder.with(getActivity())
                        .setIcon(MaterialDrawableBuilder.IconValue.PLAY)
                        .setColor(Color.WHITE)
                        .build();
                fab.setImageDrawable(drawable);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
        getActivity().getWindow().getReturnTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                FabAnimationUtils.scaleOut(fab, 50, null);
            }

            @Override
            public void onTransitionEnd(Transition transition) {

            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        album=AlbumLoader.getAlbum(getActivity(),albumID);
        setAlbumart();

        return rootView;
    }

    private void setupToolbar(){

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        final ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitle(album.title);

    }

    private void setAlbumart(){
        ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(albumID).toString(), albumArt,
                new DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnFail(R.drawable.ic_empty_music2)
                        .resetViewBeforeLoading(true)
                        .build(), new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        scheduleStartPostponedTransition(albumArt);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        Palette palette=Palette.generate(loadedImage);
                        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(Color.parseColor("#66000000")));
                        collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(Color.parseColor("#66000000")));

                        ColorStateList fabColorStateList = new ColorStateList(
                                new int[][]{
                                        new int[]{}
                                },
                                new int[] {
                                        palette.getMutedColor(Color.parseColor("#66000000")),
                                }
                        );

                        fab.setBackgroundTintList(fabColorStateList);
                        scheduleStartPostponedTransition(albumArt);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }


    });
    }

    private void setAlbumDetails(){


        LastFmClient.getInstance(getActivity()).getArtistInfo(new ArtistQuery(album.artistName), new ArtistInfoListener() {
            @Override
            public void artistInfoSucess(LastfmArtist artist) {
                ImageLoader.getInstance().displayImage(artist.mArtwork.get(1).mUrl, artistArt,
                        new DisplayImageOptions.Builder().cacheInMemory(true)
                                .cacheOnDisk(true)
                                .showImageOnFail(R.drawable.ic_empty_music2)
                                .resetViewBeforeLoading(true)
                                .displayer(new FadeInBitmapDisplayer(400))
                                .build());
            }

            @Override
            public void artistInfoFailed() {

            }
        });
        String songCount= TimberUtils.makeLabel(getActivity(),R.plurals.Nsongs,album.songCount);

        String year= (album.year!=0) ? (" - " + String.valueOf(album.year)) : "";

        albumTitle.setText(album.title);
        albumDetails.setText(album.artistName + " - " + songCount + year);



    }

    private void setUpAlbumSongs(){

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<Song> songList=AlbumSongLoader.getSongsForAlbum(getActivity(),albumID);
        mAdapter = new AlbumSongsAdapter(getActivity(), songList,albumID);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL_LIST,R.drawable.item_divider_black));
        recyclerView.setAdapter(mAdapter);

    }

    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            transition.excludeTarget(R.id.album_art, true);
            transition.setInterpolator(new LinearOutSlowInInterpolator());
            transition.setDuration(300);
            getActivity().getWindow().setEnterTransition(transition);
        }
    }


    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        getActivity().startPostponedEnterTransition();
                        return true;
                    }
                });
    }


}
