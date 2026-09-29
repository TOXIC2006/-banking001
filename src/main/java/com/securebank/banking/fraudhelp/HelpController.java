package com.securebank.banking.fraudhelp;

import com.securebank.banking.security.BankPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HelpController {
    private final HelpService help;

    public HelpController(HelpService help) {
        this.help = help;
    }

    @GetMapping("/help/tickets")
    public List<HelpService.TicketView> tickets(@AuthenticationPrincipal BankPrincipal principal) {
        return help.customerTickets(principal.customerId());
    }

    @GetMapping("/admin/fraud/cases")
    public List<FraudCase> openCases() {
        return help.openFraudCases();
    }
}
