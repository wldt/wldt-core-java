package it.wldt.storage;

import it.wldt.storage.query.QueryResourceType;

import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.error.AugmentationFunctionErrorType;
import it.wldt.augmentation.function.AugmentationFunctionType;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.request.AugmentationFunctionRequestType;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultMetrics;
import it.wldt.augmentation.result.AugmentationFunctionResultType;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.exception.StorageException;
import it.wldt.storage.model.augmentation.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AugmentationDefaultStorageTest {

    private final String DIGITAL_TWIN_ID = "dt-augmentation-storage-test";
    private final String TEST_FUNCTION_ID = "test-aug-function-id";
    private final String TEST_HANDLER_ID = "test-aug-handler-id";
    private WldtStorage wldtStorage;

    @BeforeEach
    public void initTest() {
        wldtStorage = new DefaultWldtStorage("aug_storage_test_id", true);
    }

    @AfterEach
    public void tearDown() throws StorageException {
        wldtStorage.clear();
        wldtStorage = null;
    }

    @Test
    @Order(1)
    public void testAugmentationFunctionRegistration() throws StorageException, InterruptedException {

        long startTimeStamp = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            AugmentationFunctionType type = (i % 2 == 0) ? AugmentationFunctionType.STATELESS : AugmentationFunctionType.STATEFUL;
            wldtStorage.saveAugmentationFunctionRegistration(TEST_FUNCTION_ID + "-" + i, TEST_HANDLER_ID, type);
            Thread.sleep(50);
        }

        assertEquals(50, StorageTestUtils.count(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_REGISTRATION));

        long endTimeStamp = System.currentTimeMillis();

        // Time range query
        List<AugmentationFunctionRegistrationRecord> timeRangeResult = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_REGISTRATION, startTimeStamp, endTimeStamp);
        assertNotNull(timeRangeResult);
        assertFalse(timeRangeResult.isEmpty());
        assertEquals(50, timeRangeResult.size());

        for (AugmentationFunctionRegistrationRecord record : timeRangeResult) {
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
            assertNotNull(record.getType());
        }

        // Index range query - full
        List<AugmentationFunctionRegistrationRecord> indexRangeResult = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_REGISTRATION, 0, 49);
        assertNotNull(indexRangeResult);
        assertEquals(50, indexRangeResult.size());

        for (AugmentationFunctionRegistrationRecord record : indexRangeResult) {
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
            assertNotNull(record.getType());
        }

        // Sub-range first half
        List<AugmentationFunctionRegistrationRecord> firstHalf = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_REGISTRATION, 0, 24);
        assertEquals(25, firstHalf.size());

        // Sub-range second half
        List<AugmentationFunctionRegistrationRecord> secondHalf = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_REGISTRATION, 25, 49);
        assertEquals(25, secondHalf.size());
    }

    @Test
    @Order(2)
    public void testAugmentationFunctionUnregistration() throws StorageException, InterruptedException {

        long startTimeStamp = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            AugmentationFunctionType type = (i % 2 == 0) ? AugmentationFunctionType.STATELESS : AugmentationFunctionType.STATEFUL;
            wldtStorage.saveAugmentationFunctionUnregistration(TEST_FUNCTION_ID + "-" + i, TEST_HANDLER_ID, type);
            Thread.sleep(50);
        }

        assertEquals(50, StorageTestUtils.count(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_UNREGISTRATION));

        long endTimeStamp = System.currentTimeMillis();

        // Time range query
        List<AugmentationFunctionUnregistrationRecord> timeRangeResult = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_UNREGISTRATION, startTimeStamp, endTimeStamp);
        assertNotNull(timeRangeResult);
        assertFalse(timeRangeResult.isEmpty());
        assertEquals(50, timeRangeResult.size());

        for (AugmentationFunctionUnregistrationRecord record : timeRangeResult) {
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
            assertNotNull(record.getType());
        }

        // Index range query - full
        List<AugmentationFunctionUnregistrationRecord> indexRangeResult = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_UNREGISTRATION, 0, 49);
        assertNotNull(indexRangeResult);
        assertEquals(50, indexRangeResult.size());

        for (AugmentationFunctionUnregistrationRecord record : indexRangeResult) {
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
            assertNotNull(record.getType());
        }

        // Sub-range first half
        List<AugmentationFunctionUnregistrationRecord> firstHalf = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_UNREGISTRATION, 0, 24);
        assertEquals(25, firstHalf.size());

        // Sub-range second half
        List<AugmentationFunctionUnregistrationRecord> secondHalf = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_UNREGISTRATION, 25, 49);
        assertEquals(25, secondHalf.size());
    }

    @Test
    @Order(3)
    public void testAugmentationFunctionRequest() throws StorageException, InterruptedException {

        long startTimeStamp = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            String requestId = UUID.randomUUID().toString();
            AugmentationFunctionRequestType type = (i % 2 == 0) ? AugmentationFunctionRequestType.EXECUTE : AugmentationFunctionRequestType.START;
            AugmentationFunctionRequest request = new AugmentationFunctionRequest(requestId, new AugmentationFunctionContext(), type);
            wldtStorage.saveAugmentationFunctionRequest(TEST_FUNCTION_ID, TEST_HANDLER_ID, request);
            Thread.sleep(50);
        }

        assertEquals(50, StorageTestUtils.count(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_REQUEST));

        long endTimeStamp = System.currentTimeMillis();

        // Time range query
        List<AugmentationFunctionRequestRecord> timeRangeResult = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_REQUEST, startTimeStamp, endTimeStamp);
        assertNotNull(timeRangeResult);
        assertFalse(timeRangeResult.isEmpty());
        assertEquals(50, timeRangeResult.size());

        for (AugmentationFunctionRequestRecord record : timeRangeResult) {
            assertNotNull(record.getRequestId());
            assertNotNull(record.getType());
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
        }

        // Index range query - full
        List<AugmentationFunctionRequestRecord> indexRangeResult = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_REQUEST, 0, 49);
        assertNotNull(indexRangeResult);
        assertEquals(50, indexRangeResult.size());

        for (AugmentationFunctionRequestRecord record : indexRangeResult) {
            assertNotNull(record.getRequestId());
            assertNotNull(record.getType());
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
        }

        // Sub-range first half
        List<AugmentationFunctionRequestRecord> firstHalf = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_REQUEST, 0, 24);
        assertEquals(25, firstHalf.size());

        // Sub-range second half
        List<AugmentationFunctionRequestRecord> secondHalf = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_REQUEST, 25, 49);
        assertEquals(25, secondHalf.size());
    }

    @Test
    @Order(4)
    public void testAugmentationFunctionResult() throws StorageException, InterruptedException, AugmentationFunctionException {

        long startTimeStamp = System.currentTimeMillis();

        AugmentationFunctionResultType[] resultTypes = AugmentationFunctionResultType.values();

        for (int i = 0; i < 50; i++) {
            String resultKey = "result-key-" + i;
            String resultValue = "result-value-" + i;
            AugmentationFunctionResultType resultType = resultTypes[i % resultTypes.length];

            Long startTs = System.currentTimeMillis();
            Long endTs = System.currentTimeMillis();
            AugmentationFunctionResultMetrics metrics = new AugmentationFunctionResultMetrics(startTs, endTs);

            AugmentationFunctionResult<String> result = new AugmentationFunctionResult<>(
                    resultType,
                    resultKey,
                    resultValue,
                    metrics,
                    null
            );

            // Set a request on the result so that saveAugmentationFunctionResult can read requestId
            String requestId = UUID.randomUUID().toString();
            AugmentationFunctionRequest request = new AugmentationFunctionRequest(requestId, new AugmentationFunctionContext(), AugmentationFunctionRequestType.EXECUTE);
            result.setRequest(request);

            wldtStorage.saveAugmentationFunctionResult(TEST_FUNCTION_ID, TEST_HANDLER_ID, result);
            Thread.sleep(50);
        }

        assertEquals(50, StorageTestUtils.count(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_RESULT));

        long endTimeStamp = System.currentTimeMillis();

        // Time range query
        List<AugmentationFunctionResultRecord> timeRangeResult = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_RESULT, startTimeStamp, endTimeStamp);
        assertNotNull(timeRangeResult);
        assertFalse(timeRangeResult.isEmpty());
        assertEquals(50, timeRangeResult.size());

        for (AugmentationFunctionResultRecord record : timeRangeResult) {
            assertNotNull(record.getKey());
            assertNotNull(record.getValue());
            assertNotNull(record.getType());
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
        }

        // Index range query - full
        List<AugmentationFunctionResultRecord> indexRangeResult = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_RESULT, 0, 49);
        assertNotNull(indexRangeResult);
        assertEquals(50, indexRangeResult.size());

        for (AugmentationFunctionResultRecord record : indexRangeResult) {
            assertNotNull(record.getKey());
            assertNotNull(record.getValue());
            assertNotNull(record.getType());
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
        }

        // Sub-range first half
        List<AugmentationFunctionResultRecord> firstHalf = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_RESULT, 0, 24);
        assertEquals(25, firstHalf.size());

        // Sub-range second half
        List<AugmentationFunctionResultRecord> secondHalf = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_RESULT, 25, 49);
        assertEquals(25, secondHalf.size());
    }

    @Test
    @Order(5)
    public void testAugmentationFunctionError() throws StorageException, InterruptedException {

        long startTimeStamp = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            AugmentationFunctionError error = new AugmentationFunctionError(AugmentationFunctionErrorType.ERROR, "Test error message " + i);
            wldtStorage.saveAugmentationFunctionError(TEST_FUNCTION_ID, TEST_HANDLER_ID, error);
            Thread.sleep(50);
        }

        assertEquals(50, StorageTestUtils.count(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_ERROR));

        long endTimeStamp = System.currentTimeMillis();

        // Time range query
        List<AugmentationFunctionErrorRecord> timeRangeResult = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_ERROR, startTimeStamp, endTimeStamp);
        assertNotNull(timeRangeResult);
        assertFalse(timeRangeResult.isEmpty());
        assertEquals(50, timeRangeResult.size());

        for (AugmentationFunctionErrorRecord record : timeRangeResult) {
            assertNotNull(record.getErrorId());
            assertNotNull(record.getErrorType());
            assertNotNull(record.getMessage());
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
        }

        // Index range query - full
        List<AugmentationFunctionErrorRecord> indexRangeResult = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_ERROR, 0, 49);
        assertNotNull(indexRangeResult);
        assertEquals(50, indexRangeResult.size());

        for (AugmentationFunctionErrorRecord record : indexRangeResult) {
            assertNotNull(record.getErrorId());
            assertNotNull(record.getErrorType());
            assertNotNull(record.getMessage());
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
        }

        // Sub-range first half
        List<AugmentationFunctionErrorRecord> firstHalf = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_ERROR, 0, 24);
        assertEquals(25, firstHalf.size());

        // Sub-range second half
        List<AugmentationFunctionErrorRecord> secondHalf = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_ERROR, 25, 49);
        assertEquals(25, secondHalf.size());
    }

    @Test
    @Order(6)
    public void testMixedAugmentationFunctionErrorTypes() throws StorageException, InterruptedException {

        long startTimeStamp = System.currentTimeMillis();

        AugmentationFunctionErrorType[] errorTypes = AugmentationFunctionErrorType.values();

        // Save 10 errors per type (INFO, WARNING, ERROR, CRITICAL) = 40 total
        for (AugmentationFunctionErrorType errorType : errorTypes) {
            for (int i = 0; i < 10; i++) {
                AugmentationFunctionError error = new AugmentationFunctionError(errorType, "Mixed error message " + errorType.getValue() + " " + i);
                wldtStorage.saveAugmentationFunctionError(TEST_FUNCTION_ID, TEST_HANDLER_ID, error);
                Thread.sleep(50);
            }
        }

        assertEquals(40, StorageTestUtils.count(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_ERROR));

        long endTimeStamp = System.currentTimeMillis();

        // Time range retrieval
        List<AugmentationFunctionErrorRecord> timeRangeResult = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_ERROR, startTimeStamp, endTimeStamp);
        assertNotNull(timeRangeResult);
        assertEquals(40, timeRangeResult.size());

        // Index range retrieval
        List<AugmentationFunctionErrorRecord> indexRangeResult = StorageTestUtils.inRange(wldtStorage, QueryResourceType.AUGMENTATION_FUNCTION_ERROR, 0, 39);
        assertNotNull(indexRangeResult);
        assertEquals(40, indexRangeResult.size());

        // Verify that the stored records contain all 4 error types
        Set<AugmentationFunctionErrorType> foundTypes = new HashSet<>();
        for (AugmentationFunctionErrorRecord record : indexRangeResult) {
            foundTypes.add(record.getErrorType());
        }

        for (AugmentationFunctionErrorType expectedType : errorTypes) {
            assertTrue(foundTypes.contains(expectedType),
                    "Expected error type " + expectedType + " not found in stored records");
        }

        assertEquals(4, foundTypes.size());
    }
}



