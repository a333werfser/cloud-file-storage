package edu.example.project.dto;

import edu.example.project.validation.ValidPath;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * All setters remove leading slash
 */
@Getter
@NoArgsConstructor
public class PathRequest {

    private static final String PATH_NULL = "Path must not be null";

    public interface Copy {}

    @ValidPath
    @NotNull(message = PATH_NULL)
    private String path;

    @ValidPath
    @NotNull(message = PATH_NULL, groups = Copy.class)
    private String from;

    @ValidPath
    @NotNull(message = PATH_NULL, groups = Copy.class)
    private String to;

    private String removeLeadingSlash(String value) {
        return value == null ? null : value.replaceAll("^/+", "");
    }

    /**
     * Used by Spring via reflection during @ModelAttribute binding
     */
    @SuppressWarnings("unused")
    public void setPath(String path) {
        this.path = removeLeadingSlash(path);
    }

    /**
     * Used by Spring via reflection during @ModelAttribute binding
     */
    @SuppressWarnings("unused")
    public void setFrom(String from) {
        this.from = removeLeadingSlash(from);
    }

    /**
     * Used by Spring via reflection during @ModelAttribute binding
     */
    @SuppressWarnings("unused")
    public void setTo(String to) {
        this.to = removeLeadingSlash(to);
    }

}
