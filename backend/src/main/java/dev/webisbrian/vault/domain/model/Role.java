package dev.webisbrian.vault.domain.model;

/**
 * Enum representing the role assigned to a User, controlling their access level within the vault.
 *
 * <p>Layer: domain/model — zero framework imports, pure Java only.
 */
public enum Role {
    ADMIN,
    MEMBER,
    VIEWER
}
