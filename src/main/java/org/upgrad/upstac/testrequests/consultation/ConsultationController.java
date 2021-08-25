package org.upgrad.upstac.testrequests.consultation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;


@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    Logger log = LoggerFactory.getLogger(ConsultationController.class);

    @Autowired
    private TestRequestUpdateService testRequestUpdateService;

    @Autowired
    private TestRequestQueryService testRequestQueryService;


    @Autowired
    TestRequestFlowService  testRequestFlowService;

    @Autowired
    private UserLoggedInService userLoggedInService;

    /**
     * Method to get the list of tests request for consultations.
     * @return List<TestRequest>
     */
    @GetMapping("/in-queue")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForConsultations()  {
        // Find the list of test requests having status as 'LAB_TEST_COMPLETED'
        return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);
    }

    /**
     * Method to get the list of test requests assigned to the current doctor.
     * @return List<TestRequest>
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForDoctor()  {
        // Get the logged-in doctor
        final User loggedInDoctor = userLoggedInService.getLoggedInUser();
        // Find the test requests assigned to the logged in doctor
        return testRequestQueryService.findByDoctor(loggedInDoctor);
    }

    /**
     * Method to assign test request for given id for consultation to the doctor
     * @param id of the given test request
     * @return {@link TestRequest}
     */
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/assign/{id}")
    public TestRequest assignForConsultation(@PathVariable Long id) {
        try {
            // Get the logged-in doctor
            final User loggedInDoctor = userLoggedInService.getLoggedInUser();
            // Assign the test request to the logged in doctor
            return testRequestUpdateService.assignForConsultation(id, loggedInDoctor);
        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }

    /**
     * Method to update the consultation for the given test request id with given test result for the logged in doctor.
     * @param id of the {@link TestRequest}
     * @param testResult is the test result with which consultation is to be updated with.
     * @return {@link TestRequest}
     */
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public TestRequest updateConsultation(@PathVariable Long id,@RequestBody CreateConsultationRequest testResult) {
        try {
            // Get the logged-in doctor
            final User loggedInDoctor = userLoggedInService.getLoggedInUser();
            // Update the test result for the given test request id, with given test result by the logged in doctor.
            return testRequestUpdateService.updateConsultation(id, testResult, loggedInDoctor);
        } catch (ConstraintViolationException e) {
            throw asConstraintViolation(e);
        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }

}
