package MO_system.util;

import MO_system.model.review.ApplicationItem;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;

public class RelevantSkillsDeserializer extends JsonDeserializer<List<ApplicationItem.RelevantSkill>> {

    @Override
    public List<ApplicationItem.RelevantSkill> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        return RelevantSkillsJson.parseArray(node);
    }
}
