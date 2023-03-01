package fun;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import pojo.GameResp;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GameTests {

    @Test
    public void testGameEndpoint() throws IOException, ProcessingException {
        String[] game = {"Chess", "Solitaire"};

        for (String gameVer : game) {
            Response response = RestAssured.get("http://localhost:8080/game?name=" + gameVer);

            GameResp respgame = response.as(GameResp.class);

            response.then()
                    .log().all()
                    .assertThat()
                    .statusCode(200)
                    .body("id", greaterThan(0))
                    .body("text", equalTo("This is " + gameVer));

            assert(respgame.getId() > 0);
            assert(respgame.getText().equals("This is " + gameVer));


            JsonSchemaFactory schemafac = JsonSchemaFactory.byDefault();
            JsonNode schemaNode = JsonLoader.fromFile(new File("src/gameResponseSchema.json"));
            JsonSchema schema = schemafac.getJsonSchema(schemaNode);

            JsonNode respJson = JsonLoader.fromString(response.getBody().asString());
            ProcessingReport report = schema.validate(respJson);
            assertTrue(report.isSuccess(), "Response :\n" + report);

        }

    }

}

