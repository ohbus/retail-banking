package xyz.subho.retail.banking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import xyz.subho.retail.banking.model.Recipient;
import xyz.subho.retail.banking.model.User;
import xyz.subho.retail.banking.service.TransactionService;
import xyz.subho.retail.banking.service.UserService;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @InjectMocks
    private TransferController controller;

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserService userService;

    @Test
    void recipientEditUsesAuthenticatedOwner() {
        Principal principal = () -> "alice";
        Recipient recipient = new Recipient();
        when(transactionService.findRecipientByName("Bob", "alice")).thenReturn(recipient);
        when(transactionService.findRecipientList(principal)).thenReturn(List.of(recipient));

        assertEquals("recipient", controller.recipientEdit("Bob", new ExtendedModelMap(), principal));
        verify(transactionService).findRecipientByName("Bob", "alice");
    }

    @Test
    void recipientDeleteIsOwnerScoped() {
        Principal principal = () -> "alice";
        when(transactionService.findRecipientList(principal)).thenReturn(List.of());

        assertEquals("recipient", controller.recipientDelete("Bob", new ExtendedModelMap(), principal));
        verify(transactionService).deleteRecipientByName("Bob", "alice");
    }

    @Test
    void recipientSaveBindsAuthenticatedUser() {
        Principal principal = () -> "alice";
        User user = new User();
        user.setUsername("alice");
        Recipient recipient = new Recipient();
        when(userService.findByUsername("alice")).thenReturn(user);

        assertEquals("redirect:/transfer/recipient", controller.recipientPost(recipient, principal));
        verify(transactionService).saveRecipient(recipient, "alice");
    }
}
