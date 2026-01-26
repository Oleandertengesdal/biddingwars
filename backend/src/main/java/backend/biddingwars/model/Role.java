package backend.biddingwars.model;

public enum Role {
public enum Role implements GrantedAuthority {
    USER,
    ADMIN
}
