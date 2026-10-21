package io.github.oldmerman.core;

import io.github.oldmerman.core.api.chat.ChatClient;
import io.github.oldmerman.core.api.provider.ProviderClient;
import io.github.oldmerman.core.api.search.SearchClient;
import io.github.oldmerman.core.config.DeepSeekConfig;

public class DeepSeekClient {

    public static ChatClient chat(DeepSeekConfig config) {
        return new ChatClient(config);
    }

    public static ProviderClient provider(DeepSeekConfig config) {
        return new ProviderClient(config);
    }

    public static SearchClient search(DeepSeekConfig config) {
        return new SearchClient(config);
    }
}
