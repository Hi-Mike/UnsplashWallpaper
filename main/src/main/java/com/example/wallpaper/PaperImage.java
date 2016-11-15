package com.example.wallpaper;

/**
 * Created by ske on 2016/11/15.
 */

public class PaperImage {
    private String id;
    private ImageUrl urls;
    private Link links;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ImageUrl getUrls() {
        return urls;
    }

    public void setUrls(ImageUrl urls) {
        this.urls = urls;
    }

    public Link getLinks() {
        return links;
    }

    public void setLinks(Link links) {
        this.links = links;
    }

    public class ImageUrl {
        private String raw;
        private String full;
        private String regular;

        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }

        public String getFull() {
            return full;
        }

        public void setFull(String full) {
            this.full = full;
        }

        public String getRegular() {
            return regular;
        }

        public void setRegular(String regular) {
            this.regular = regular;
        }
    }

    public class Link {
        private String download;
        private String download_location;

        public String getDownload() {
            return download;
        }

        public void setDownload(String download) {
            this.download = download;
        }

        public String getDownload_location() {
            return download_location;
        }

        public void setDownload_location(String download_location) {
            this.download_location = download_location;
        }
    }
}
