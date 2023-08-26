package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile){
        User u=new User(name,mobile);
        if(users.contains(u)) return u;
        users.add(u);
        userPlaylistMap.put(u,new ArrayList<>());
        return u;
    }

    public Artist createArtist(String name) {

        Artist a=new Artist(name);
        if(artists.contains(a)) return a;
        artists.add(a);
        return a;
    }

    public Album createAlbum(String title, String artistName) {
        Album a=new Album(title);
        if(!albumSongMap.containsKey(a)) albumSongMap.put(a,new ArrayList<>());
        Artist ar=createArtist(artistName);
        if(!albums.contains(a)) albums.add(a);
        if(!artistAlbumMap.containsKey(ar)) artistAlbumMap.put(ar,new ArrayList<>());
        if(!artistAlbumMap.get(ar).contains(a)) artistAlbumMap.get(ar).add(a);
        return a;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Song s=new Song(title,length);
        if(!songs.contains(s)) {
            songs.add(s);
            songLikeMap.put(s,new ArrayList<>());
        }
        Album a=new Album(albumName);
        if(!albums.contains(a) || !albumSongMap.containsKey(a))
       throw new Exception("Album does not exist");
        if(!albumSongMap.get(a).contains(s)) albumSongMap.get(a).add(s);
        return s;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        Playlist p=new Playlist(title);
        if(!playlists.contains(p)) playlists.add(p);
        User u=new User(title,mobile);
        if(!users.contains(u)){
            throw new Exception("User does not exist");
        }
        if(!playlistListenerMap.containsKey(p)) playlistListenerMap.put(p,new ArrayList<>());
        playlistListenerMap.get(p).add(u);
        List<Song> l=new ArrayList<>();
        for(Song s:songs){
            if(s.getLength()==length) l.add(s);
        }
        if(!playlistSongMap.containsKey(p)) playlistSongMap.put(p,l);
        if(!creatorPlaylistMap.containsKey(u)) creatorPlaylistMap.put(u,p);
        userPlaylistMap.get(u).add(p);
        return p;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        Playlist p=new Playlist(title);
        if(!playlists.contains(p)) playlists.add(p);
        User u=new User(title,mobile);
        if(!users.contains(u)){
            throw new Exception("User does not exist");
        }
        if(!playlistListenerMap.containsKey(p)) playlistListenerMap.put(p,new ArrayList<>());
        playlistListenerMap.get(p).add(u);
        List<Song> l=new ArrayList<>();
        for(Song s:songs){
            for(String ss:songTitles){
                if(s.getTitle()==ss) l.add(s);
            }
        }
        if(!playlistSongMap.containsKey(p)) playlistSongMap.put(p,l);
        if(!creatorPlaylistMap.containsKey(u)) creatorPlaylistMap.put(u,p);
        userPlaylistMap.get(u).add(p);
        return p;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
         User u=new User(playlistTitle,mobile);
         if(!users.contains(u)){
             throw new Exception("User does not exist");
         }
         Playlist p=new Playlist(playlistTitle);
        if(!playlists.contains(p)){
            throw new Exception("Playlist does not exist");
        }
        if(creatorPlaylistMap.containsKey(u) && creatorPlaylistMap.get(u).equals(p)) return null;
        if(playlistListenerMap.containsKey(p) && playlistListenerMap.get(p).contains(u)) return null;
        if(!playlistListenerMap.get(p).contains(u))  playlistListenerMap.get(p).add(u);
        userPlaylistMap.get(u).add(p);


        return p;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        if(mobile==null || songTitle==null) throw new NullPointerException();
        Song s=null;
        for(Song ss:songs){
            if(ss.getTitle().equals(songTitle)){
                s=ss;
                break;
            }
        }
        if(s==null){
            throw new Exception("Song does not exist");
        }
        User u=null;
        for(User uu:users){
            if(uu.getMobile().equals(mobile)){
                u=uu;
                break;
            }
        }
        if(u==null){
            throw new Exception("User does not exist");
        }
        if(!songLikeMap.get(s).contains(u)) {
            songLikeMap.get(s).add(u);
            int likes=s.getLikes();
            s.setLikes(likes+1);

            Album a=null;
            for(Album aa:albumSongMap.keySet()){
                if(albumSongMap.get(aa).contains(s)){
                    a=aa;
                    break;
                }
            }
            if(a==null) throw new Exception("Album does not exist");

            Artist ar=null;
            for(Artist art:artistAlbumMap.keySet()){
                if(artistAlbumMap.get(art).contains(a)){
                    ar=art;
                    break;
                }
            }
            if(ar==null) throw new Exception("Artist does not exist");
            int artLikes=ar.getLikes();
            ar.setLikes(artLikes+1);

        }
        return s;

    }

    public String mostPopularArtist(){
        int likes=Integer.MIN_VALUE;
        Artist ar=null;
        for(Artist a:artists){
            if(a.getLikes()>likes){
                ar=a;
                likes=a.getLikes();
            }
        }
       if(ar!=null) return ar.getName();
       return "Not Found";
    }

    public String mostPopularSong() {
        int likes=Integer.MIN_VALUE;
        Song ar=null;
        for(Song a:songs){
            if(a.getLikes()>likes){
                ar=a;
                likes=a.getLikes();
            }
        }
        if(ar!=null) return ar.getTitle();
        return "Not Found";
    }
}
