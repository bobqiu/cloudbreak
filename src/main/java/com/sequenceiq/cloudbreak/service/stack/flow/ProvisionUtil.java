package com.sequenceiq.cloudbreak.service.stack.flow;

import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.sequenceiq.cloudbreak.controller.BuildStackFailureException;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.logger.MDCBuilder;
import com.sequenceiq.cloudbreak.repository.StackRepository;

@Component
public class ProvisionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisionUtil.class);

    @Autowired
    private StackRepository stackRepository;

    public synchronized boolean isRequestFull(Stack stack, int fullIndex, int multiplier) {
        return (fullIndex * multiplier) % stack.cloudPlatform().parallelNumber() == 0;
    }

    public synchronized void waitForRequestToFinish(Long stackId, List<Future<Boolean>> futures) {
        Stack stack = stackRepository.findOneWithLists(stackId);
        MDCBuilder.buildMdcContext(stack);
        LOGGER.info("Waiting for futures to finishing.");
        StringBuilder sb = new StringBuilder();
        Optional<Exception> exception = Optional.absent();
        for (Future<Boolean> future : futures) {
            try {
                future.get();
            } catch (Exception ex) {
                exception = Optional.fromNullable(ex);
                sb.append(String.format("%s, ", ex.getMessage()));
            }
        }
        if (exception.isPresent()) {
            throw new BuildStackFailureException(sb.toString(), exception.orNull(), stack.getResources());
        }
        LOGGER.info("All futures finished continue with the next group.");
    }

}
