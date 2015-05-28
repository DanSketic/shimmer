package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;


/**
 * A set of utility methods to help with mapping {@link JsonNode} objects.
 *
 * @author Emerson Farrugia
 */
public class JsonNodeMappingSupport {

    private static final Logger logger = LoggerFactory.getLogger(JsonNodeMappingSupport.class);


    /**
     * @param parentNode a parent node
     * @param path a path to a child node
     * @return the child node reached by traversing the path
     * @throws JsonNodeMappingException if the child node doesn't exist
     */
    public static JsonNode asRequiredNode(JsonNode parentNode, String path) {

        if (!parentNode.hasNonNull(path)) {
            throw new JsonNodeMappingException(format("A '%s' field wasn't found in node '%s'.", path, parentNode));
        }

        return parentNode.path(path);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @param typeChecker the function to check if the type is compatible
     * @param converter the function to convert the node to a value
     * @param <T> the type of the value to convert to
     * @return the value of the child node
     * @throws JsonNodeMappingException if the child doesn't exist or if the value of the child node isn't compatible
     */
    public static <T> T asRequiredValue(JsonNode parentNode, String path, Function<JsonNode, Boolean> typeChecker,
            Function<JsonNode, T> converter) {

        JsonNode childNode = asRequiredNode(parentNode, path);

        if (!typeChecker.apply(childNode)) {
            throw new JsonNodeMappingException(
                    format("The '%s' field in node '%s' isn't compatible.", path, parentNode));
        }

        return converter.apply(childNode);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a string
     * @throws JsonNodeMappingException if the child doesn't exist or if the value of the child node isn't textual
     */
    public static String asRequiredString(JsonNode parentNode, String path) {

        return asRequiredValue(parentNode, path, JsonNode::isTextual, JsonNode::textValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a long
     * @throws JsonNodeMappingException if the child doesn't exist or if the value of the child node isn't an integer
     */
    public static Long asRequiredLong(JsonNode parentNode, String path) {

        return asRequiredValue(parentNode, path, JsonNode::isIntegralNumber, JsonNode::longValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @param typeChecker the function to check if the type is compatible
     * @param converter the function to convert the node to a value
     * @param <T> the type of the value to convert to
     * @return the value of the child node, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't compatible
     */
    public static <T> Optional<T> asOptionalValue(JsonNode parentNode, String path,
            Function<JsonNode, Boolean> typeChecker, Function<JsonNode, T> converter) {

        JsonNode childNode = parentNode.path(path);

        if (childNode.isMissingNode()) {
            logger.warn("A '{}' field wasn't found in node '{}'.", path, parentNode);
            return Optional.empty();
        }

        if (childNode.isNull()) {
            return Optional.empty();
        }

        if (!typeChecker.apply(childNode)) {
            logger.warn("The '{}' field in node '{}' isn't compatible.", path, parentNode);
            return Optional.empty();
        }

        return Optional.of(converter.apply(childNode));
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a string, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't textual
     */
    public static Optional<String> asOptionalString(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isTextual, JsonNode::textValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a date time, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't a date time
     */
    public static Optional<OffsetDateTime> asOptionalOffsetDateTime(JsonNode parentNode, String path) {

        Optional<String> string = asOptionalString(parentNode, path);

        if (!string.isPresent()) {
            return Optional.empty();
        }

        OffsetDateTime dateTime = null;

        try {
            dateTime = OffsetDateTime.parse(string.get());
        }
        catch (DateTimeParseException e) {
            logger.warn("The '{}' field in node '{}' with value '{}' isn't a valid timestamp.",
                    path, parentNode, string.get(), e);
        }

        return Optional.ofNullable(dateTime);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a double, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't numeric
     */
    public static Optional<Double> asOptionalDouble(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isNumber, JsonNode::doubleValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a long, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't an integer
     */
    public static Optional<Long> asOptionalLong(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isIntegralNumber, JsonNode::longValue);
    }
}
