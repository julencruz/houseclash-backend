package com.houseclash.backend.infrastructure.scheduler

import com.houseclash.backend.domain.port.HouseRepository
import com.houseclash.backend.domain.usecase.ApplyMarketInflationUsecase
import com.houseclash.backend.domain.usecase.AutoApproveExpiredTasksUsecase
import com.houseclash.backend.domain.usecase.PurgeActivityLogUsecase
import com.houseclash.backend.domain.usecase.RecurringTaskSchedulerUsecase
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class TaskSchedulerService(
    private val recurringTaskSchedulerUsecase: RecurringTaskSchedulerUsecase,
    private val autoApproveExpiredTasksUsecase: AutoApproveExpiredTasksUsecase,
    private val applyMarketInflationUsecase: ApplyMarketInflationUsecase,
    private val purgeActivityLogUsecase: PurgeActivityLogUsecase,
    private val houseRepository: HouseRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 1)
    fun scheduleRecurringTasks() {
        logger.info("Scheduler: starting recurring tasks generation")
        recurringTaskSchedulerUsecase.execute()
        logger.info("Scheduler: recurring tasks generation completed")
    }

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 1)
    fun autoApproveExpiredTasks() {
        val houses = houseRepository.findAll()
        logger.info("Scheduler: auto-approving expired tasks for {} houses", houses.size)
        houses.forEach { house ->
            logger.debug("Scheduler: auto-approving expired tasks for house {}", house.id)
            autoApproveExpiredTasksUsecase.execute(house.id!!)
        }
        logger.info("Scheduler: auto-approve expired tasks completed")
    }

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 6)
    fun applyMarketInflation() {
        val houses = houseRepository.findAll()
        logger.info("Scheduler: applying market inflation for {} houses", houses.size)
        houses.forEach { house ->
            logger.debug("Scheduler: applying market inflation for house {}", house.id)
            applyMarketInflationUsecase.execute(house.id!!)
        }
        logger.info("Scheduler: market inflation applied")
    }

    @Scheduled(cron = "0 0 0 * * MON")
    fun purgeActivityLogs() {
        val houses = houseRepository.findAll()
        logger.info("Scheduler: purging activity logs for {} houses", houses.size)
        houses.forEach { house ->
            logger.debug("Scheduler: purging activity log for house {}", house.id)
            purgeActivityLogUsecase.execute(house.id!!)
        }
        logger.info("Scheduler: activity log purge completed")
    }
}
