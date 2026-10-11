package xyz.subho.retail.banking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.isNull;

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
        when(transactionService.findRecipientById(1L, "alice")).thenReturn(recipient);
        when(transactionService.findRecipientList(principal)).thenReturn(List.of(recipient));

        assertEquals("recipient", controller.recipientEdit(1L, new ExtendedModelMap(), principal));
        verify(transactionService).findRecipientById(1L, "alice");
    }

    @Test
    void recipientDeleteIsOwnerScoped() {
        Principal principal = () -> "alice";
        when(transactionService.findRecipientList(principal)).thenReturn(List.of());

        assertEquals("recipient", controller.recipientDelete(1L, new ExtendedModelMap(), principal));
        verify(transactionService).deleteRecipientById(1L, "alice");
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

    @Test
    void invalidBetweenAccountTransferReturnsForm() {
        Principal principal = () -> "alice";
        User user = new User();
        when(userService.findByUsername("alice")).thenReturn(user);
        doThrow(new IllegalArgumentException("Invalid transfer")).when(transactionService)
                .betweenAccountsTransfer("Current", "Savings", "bad", null, null);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("betweenAccounts",
                controller.betweenAccountsPost("Current", "Savings", "bad", principal, model));
        assertEquals("Invalid transfer", model.getAttribute("error"));
    }

    @Test
    void invalidRecipientTransferReturnsForm() {
        Principal principal = () -> "alice";
        User user = new User();
        when(userService.findByUsername("alice")).thenReturn(user);
        when(transactionService.findRecipientById(1L, "alice")).thenReturn(new Recipient());
        doThrow(new IllegalArgumentException("Amount is invalid")).when(transactionService)
                .toSomeoneElseTransfer(org.mockito.ArgumentMatchers.any(Recipient.class),
                        org.mockito.ArgumentMatchers.eq("Current"), org.mockito.ArgumentMatchers.eq("bad"),
                        isNull(), isNull());
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("toSomeoneElse",
                controller.toSomeoneElsePost(1L, "Current", "bad", principal, model));
        assertEquals("Amount is invalid", model.getAttribute("error"));
    }
}
