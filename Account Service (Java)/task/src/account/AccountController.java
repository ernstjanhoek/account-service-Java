package account;

import account.AccountServiceExceptions.AdminDeletionException;
import account.AccountServiceExceptions.BreachedPasswordException;
import account.AccountServiceExceptions.PasswordEqualsException;
import account.AccountServiceExceptions.UserExistsException;
import account.DTO.*;
import account.Entities.Salary;
import account.Entities.SalaryID;
import account.Entities.AccountGroups;
import account.Entities.SecurityEvent;
import account.repositories.SalaryRepository;
import account.repositories.AccountGroupsRepository;
import account.repositories.UserRepository;
import account.repositories.SecurityEventRepository;
import account.usermanager.User;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

@Validated
@RestController
public class AccountController {
    private final List<AccountGroups> businessRoles = List.of(
            new AccountGroups("ROLE_ACCOUNTANT"),
            new AccountGroups("ROLE_AUDITOR"),
            new AccountGroups("ROLE_USER")
    );
    private final List<String> breachedPasswordsRepository = Collections.synchronizedList(
            new ArrayList<>(Arrays.asList(
                    "PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
                    "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                    "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember")
            ));
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SalaryRepository salaryRepository;
    private final AccountGroupsRepository accountGroupsRepository;
    private final SecurityEventRepository securityEventRepository;

    AccountController(PasswordEncoder passwordEncoder,
                      UserRepository userRepository,
                      SalaryRepository salaryRepository,
                      AccountGroupsRepository accountGroupsRepository,
                      SecurityEventRepository securityEventRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.salaryRepository = salaryRepository;
        this.accountGroupsRepository = accountGroupsRepository;
        this.securityEventRepository = securityEventRepository;
    }

    @PostMapping("/api/auth/signup")
    public SignupResponse postSignup(@Valid @RequestBody SignupRequest request) {
        if (breachedPasswordsRepository.contains(request.getPassword())) {
            throw new BreachedPasswordException();
        }
        AccountGroups defaultRole = accountGroupsRepository.findById("ROLE_USER").get();
        AccountGroups administrator = accountGroupsRepository.findById("ROLE_ADMINISTRATOR").get();
        AccountGroups role = userRepository.count() == 0 ? administrator : defaultRole;
        User userObj = new User(
                request.getEmail().toLowerCase(),
                request.getName(),
                request.getLastname(),
                passwordEncoder.encode(request.getPassword()),
                List.of(role)
        );
        try {
            userRepository.save(userObj);
            securityEventRepository.save(
                    new SecurityEvent(
                            LocalDate.now(),
                            "CREATE_USER",
                            userObj.getEmail(),
                            "/api/auth/signup"
                    )
            );
        } catch (Exception e) {
            throw new UserExistsException();
        }
        return new SignupResponse(userObj.getId(), request.getName(), request.getLastname(), request.getEmail(), userObj.getRoles().stream().map(AccountGroups::getRole).toList());
    }

    @PostMapping("/api/auth/changepass")
    public ChangePassResponse postChangePass(@AuthenticationPrincipal UserDetails userDetails,
                                             @Valid @RequestBody ChangePassRequest request) {
        if (breachedPasswordsRepository.contains(request.getNewPassword())) {
            throw new BreachedPasswordException();
        }
        Optional<User> optionalUser = userRepository.findUserByEmail(userDetails.getUsername().toLowerCase());
        optionalUser.ifPresentOrElse(u -> {
            if (passwordEncoder.matches(request.getNewPassword(), u.getPassword())) {
                throw new PasswordEqualsException();
            }
            u.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.updateUserByEmail(
                    userDetails.getUsername().toLowerCase(),
                    u
            );
            securityEventRepository.save(
                    new SecurityEvent(
                            LocalDate.now(),
                            "CHANGE_PASSWORD",
                            u.getEmail(),
                            u.getEmail(),
                            "/api/auth/changepass"
                    )
            );
        }, () -> {
            throw new EntityNotFoundException();
        });
        return new ChangePassResponse(
                userDetails.getUsername().toLowerCase(),
                "The password has been updated successfully"
        );
    }

    @PostMapping("/api/acct/payments")
    public StatusResponse postSalary(@Valid @RequestBody List<@Valid SalaryRequest> request) {
        List<Salary> salaryArrayList = request.stream().map(v -> {
            Salary salaryObj = new Salary();
            salaryObj.setPeriod(v.getDateFromString());
            salaryObj.setSalary(v.getSalary());
            userRepository.findUserByEmail(v.getEmployee().toLowerCase()).ifPresentOrElse(r -> {
                salaryObj.setUser(r);
                if (salaryRepository.existsById(new SalaryID(r.getId(), v.getDateFromString()))) {
                    throw new EntityExistsException();
                }
            }, () -> {
                throw new EntityNotFoundException("User does not exist");
            });
            return salaryObj;
        }).toList();

        salaryRepository.saveAll(salaryArrayList);
        return new StatusResponse("Added successfully!");
    }

    @PutMapping("/api/acct/payments")
    public StatusResponse putSalary(@Valid @RequestBody SalaryRequest request) {
        Optional<User> user = userRepository.findUserByEmail(request.getEmployee().toLowerCase());
        user.ifPresentOrElse(v -> {
            Salary salary = new Salary();
            salary.setPeriodFromString(request.getPeriod());
            salary.setSalary(request.getSalary());
            salary.setUser(v);
            salaryRepository.save(salary);
        }, () -> {
            throw new EntityNotFoundException("User does not exist");
        });
        return new StatusResponse("Updated successfully!");
    }

    @GetMapping("/api/empl/payment")
    public ResponseEntity<?> getPayment(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestParam(required = false) @Valid @Pattern(regexp = "(^(0[1-9]|1[0-2])-(19|20)\\d{2}$)") String period) throws ParseException {
        if (period != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-yyyy");
            Date date = dateFormat.parse(period);
            PaymentResponse response = new PaymentResponse();
            Optional<User> user = userRepository.findUserByEmail(userDetails.getUsername().toLowerCase());
            user.ifPresent(u -> salaryRepository.findById(new SalaryID(u.getId(), date)).ifPresent(r -> {
                        response.setPeriod(r.getPeriod());
                        response.setSalary(r.getSalary());
                        response.setLastname(u.getLastname());
                        response.setName(u.getName());
                    }
            ));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            ArrayList<PaymentResponse> response = new ArrayList<>();
            Optional<User> user = userRepository.findUserByEmail(userDetails.getUsername().toLowerCase());
            user.ifPresent(v -> salaryRepository.findAllByUserIdOrderByPeriodDesc(v.getId()).forEach(r -> {
                PaymentResponse result = new PaymentResponse();
                result.setName(v.getName());
                result.setLastname(v.getLastname());
                result.setPeriod(r.getPeriod());
                result.setSalary(r.getSalary());
                response.add(result);
            }));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @PutMapping("api/admin/user/role")
    public ResponseEntity<?> putRole(@Valid @RequestBody RoleRequest request,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> dbUser = userRepository.findUserByEmail(request.getUser());
        Optional<AccountGroups> dbRole = accountGroupsRepository.findById("ROLE_" + request.getRole());
        if (dbRole.isEmpty()) {
            throw new EntityNotFoundException("Role not found!");
        }
        AccountGroups role = dbRole.get();
        AtomicReference<User> returnObj = new AtomicReference<>(new User());
        dbUser.ifPresentOrElse(a -> {
            Collection<AccountGroups> roles = a.getRoles();
            String operation =  request.getOperation();
            if (operation.equals("REMOVE")) {
                if (!roles.contains(role)) {
                    throw new DataIntegrityViolationException("The user does not have a role!");
                }
                if (role.getRole().equals("ROLE_ADMINISTRATOR")) {
                    throw new IllegalArgumentException("Can't remove ADMINISTRATOR role!");
                }
                if (roles.size() == 1) {
                    throw new IllegalArgumentException("The user must have at least one role!");
                }
                roles.remove(role);
                a.setRoles(roles);
                userRepository.save(a);
                securityEventRepository.save(
                        new SecurityEvent(
                                LocalDate.now(),
                                "REMOVE_ROLE",
                                userDetails.getUsername(),
                                "Remove role " + request.getRole() + " from " + a.getEmail(),
                                "/api/admin/user/role"
                        )
                );
                returnObj.set(a);
            } else if (operation.equals("GRANT")) {
                if (roles.contains(role)) {
                    throw new DataIntegrityViolationException("User already has a role!");
                }
                if (!businessRoles.contains(role) || roles.contains(new AccountGroups("ROLE_ADMINISTRATOR"))) {
                    throw new IllegalArgumentException("The user cannot combine administrative and business roles!");
                }
                roles.add(role);
                a.setRoles(roles);
                userRepository.save(a);

                securityEventRepository.save(
                        new SecurityEvent(
                                LocalDate.now(),
                                "GRANT_ROLE",
                                userDetails.getUsername(),
                                "Grant role " + request.getRole() + " to " + a.getEmail(),
                                "/api/admin/user/role"
                        )
                );
                returnObj.set(a);
            }
        }, () -> {
            throw new EntityNotFoundException("User not found!");
        });
        User unpackedObj = returnObj.get();
        return new ResponseEntity<>(
                new SignupResponse(
                        unpackedObj.getId(),
                        unpackedObj.getName(),
                        unpackedObj.getLastname(),
                        unpackedObj.getEmail(),
                        unpackedObj.getRoles().stream().map(AccountGroups::getRole).sorted().toList()
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("api/admin/user/")
    public ResponseEntity<?> getRoles() {
        Iterable<User> dbResult = userRepository.findAll();
        ArrayList<SignupResponse> response = new ArrayList<>();
        dbResult.forEach(a -> {
            response.add(new SignupResponse(
                    a.getId(),
                    a.getName(),
                    a.getLastname(),
                    a.getEmail(),
                    a.getRoles().stream().map(AccountGroups::getRole).toList()
            ));
        });
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("api/admin/user/{email}")
    public StatusResponseUser deleteRole(@PathVariable String email,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> dbResult = userRepository.findUserByEmail(email);
        if (dbResult.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        dbResult.ifPresent(a -> {
            if (a.getRoles().stream().map(AccountGroups::getRole).noneMatch("ROLE_ADMINISTRATOR"::equals)) {
                userRepository.deleteById(a.getId().intValue());
                securityEventRepository.save(
                        new SecurityEvent(
                                LocalDate.now(),
                                "DELETE_USER",
                                userDetails.getUsername(),
                                a.getEmail(),
                                "/api/admin/user"
                        )
                );
            } else {
                throw new AdminDeletionException("Can't remove ADMINISTRATOR role!");
            }
        });
        return new StatusResponseUser(dbResult.get().getEmail(), "Deleted successfully!");
    }

    @PutMapping("api/admin/user/access")
    public ResponseEntity<StatusResponse> putAccess(@Valid @RequestBody AccessRequest request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> dbResult = userRepository.findUserByEmail(request.getUser().toLowerCase());
        if (dbResult.isEmpty()) {
            throw new EntityNotFoundException("User does not exist");
        }
        if (dbResult.get().getLocked().equals(request.isLocked())) {
            throw new IllegalArgumentException("Lock state is already set.");
        }
        if (dbResult.get().getRoles().contains(new AccountGroups("ROLE_ADMINISTRATOR"))) {
            throw new IllegalArgumentException("Can't lock the ADMINISTRATOR!");
        }
        User userObj = dbResult.get();
        userObj.switchState();
        userObj.setFailedLoginAttempts(0);
        userRepository.save(userObj);
        String eventObjectString = request.getOperation().equals("LOCK") ? "Lock user " : "Unlock user ";

        securityEventRepository.save(
                new SecurityEvent(
                        LocalDate.now(),
                        request.getOperation() + "_USER",
                        userDetails.getUsername(),
                        eventObjectString + userObj.getEmail(),
                        "/api/admin/user/access"
                )
        );
        String statusStr = request.getOperation().equals("LOCK") ? "locked" : "unlocked";

        return new ResponseEntity<>(
                new StatusResponse("User " +
                        request.getUser().toLowerCase() +
                        " " + statusStr + "!"
                ),
                HttpStatus.OK
        );
    }
    @GetMapping("api/security/events/")
    public ResponseEntity<List<EventResponse>> getEvents() {
        ArrayList<EventResponse> response = new ArrayList<>();
        Iterable<SecurityEvent> dbResults = securityEventRepository.findAll();
        dbResults.forEach(a -> {
            response.add(new EventResponse(
                    a.getId(),
                    a.getDate().toString(),
                    a.getAction(),
                    a.getSubject(),
                    a.getObject(),
                    a.getPath()
            ));
        });

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}