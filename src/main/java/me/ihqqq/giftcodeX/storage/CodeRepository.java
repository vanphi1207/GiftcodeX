package me.ihqqq.giftcodeX.storage;

import me.ihqqq.giftcodeX.model.Giftcode;

import java.util.Map;

public interface CodeRepository {

    Map<String, Giftcode> loadAll();

    void saveAll(Map<String, Giftcode> codes);

    void reload();
}