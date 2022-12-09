package User;

import Entities.User;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTests {
    private static User user;
    public static Faker faker;
    public static RequestSpecification request;
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://petstore.swagger.io/v2";

        faker = new Faker();


        //user = new User("Grazi", "Grazielle", "Pinheiro", "email@teste.com", "12345", "22334455");

        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8,10),
                faker.phoneNumber().toString());
    }

    @BeforeEach
    public void setRequest(){
        request = given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
        .header("api-key", "special-key")
                        .contentType(ContentType.JSON);

    }

    @Test
    @Order(1)
    public void CreateNewUser_WithValidData_ReturnOk(){

        request
            .body(user)
            .when()
            .post("/user")
            .then()
            .assertThat().statusCode(200).and()
            .body("code", equalTo(200))
            .body("type", equalTo("unknown"))
            .body("message", isA(String.class))
            .body("size()", equalTo(3));
    }


    @Test
    @Order(2)
    public void GetLogin_InvalidUser_ReturnOk(){

        request 
            .param("username", user.getUsername())
            .param("password", user.getPassword())
            .when()
            .get("/user/login")
            .then()
            .assertThat()
            .statusCode(200)
            .and().time(lessThan(3000L));
    }

    @Test
    @Order(3)
    public void GetUserByUsername_userIsValid_ReturnOk(){

        request
            .when()
            .get("/user/" + user.getUsername())
            .then()
            .assertThat().statusCode(200).and().time(lessThan(2000L))
            .and().body("username", equalTo(user.getUsername()));


    }
    
    @Test
    @Order(4)
    public void DeleteUser_UserExists_ReturnOk(){

        request 
            .when()
            .delete("/user/" + user.getUsername())
            .then()
            .assertThat().statusCode(200).and().time(lessThan(2000L));
    }

    //Usuario invalido
    @Test
    @Order(5)
    public void CreateNewUser_WithInvalidBody_ReturnBadRequest() {

        Response response = request
                .body("teste")
                .when()
                .post("/user")
                .then()
                .extract().response();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals(true, response.getBody().asPrettyString().contains("unknown"));
        Assertions.assertEquals(3, response.body().jsonPath().getMap("$").size());
    }

    @Test
    @Order(6)
    public void UpdateUser_RetunrOK(){

        request
            .body(user)
            .when()
            .put("/user/" + user.getUsername())
            .then()
            .assertThat().statusCode(200).time(lessThan(3000L))
            .and().body("type", equalTo("unknown"))
            .body("message", isA(String.class));     
    }

    @Test
    @Order(7)
    public void Log_out_session(){

        request
            .when()
            .get("/user/logout")
            .then()
            .assertThat().statusCode(200).time(lessThan(2000L))
            .and().body("type", equalTo("unknown"))
            .body("message", isA(String.class));     
    }



}
