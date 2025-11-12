package dat.dtos;

public class RegisterResponse {
    public Long userId;
    public Long customerId;
    public Long serialLinkId;
    public Long planId;
    public String message;


    public RegisterResponse(Long userId, Long customerId, Long serialLinkId, Long planId, String message) {
        this.userId = userId;
        this.customerId = customerId;
        this.serialLinkId = serialLinkId;
        this.planId = planId;
        this.message = message;
    }

}
