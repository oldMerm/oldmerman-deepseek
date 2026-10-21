package io.github.oldmerman.core.api.chat.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.oldmerman.core.enums.ImageDetail;

public record ImagePart(String type, @JsonProperty("image_url") Image imageUrl) implements ContentPart {

    public static ImagePart fromBase64(String base64){
        return new ImagePart("image_url", new Image("data:image/jpeg;base64," + base64, null));
    }

    public static ImagePart fromBase64(String base64, ImageDetail detail){
        return new ImagePart("image_url", new Image("data:image/jpeg;base64," + base64, detail));
    }

    public static ImagePart fromUrl(String url){
        return new ImagePart("image_url", new Image(url, null));
    }

    public static ImagePart fromUrl(String url, ImageDetail detail){
        return new ImagePart("image_url", new Image(url, detail));
    }

}
