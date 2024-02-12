```mermaid
sequenceDiagram
    participant scenario
    box web
        participant http
    end
    box communication
        participant KnownDevicesReadModel
    end
    box installations
        participant InstallationService
        participant InstallationReadModel
        participant scenario
    end
    box mediators
        participant InstallationsToDevicesMediator
    end
    box device
        participant DeviceService
    end
    box search
        participant DevicesReadModel
    end
    box persistence
        participant Repository
    end
    scenario ->>+ InstallationService: handleWorkOrder
    InstallationService ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>- InstallationService: return save
    InstallationService ->>+ InstallationReadModel: projectionOf
    InstallationReadModel ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>+ Repository: SELECT installation
    Repository ->>- Repository: return SELECT installation
    Repository ->>- Repository: return Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return findById
    InstallationReadModel ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>+ Repository: SELECT installation
    Repository ->>- Repository: return SELECT installation
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return save
    InstallationReadModel ->>- InstallationService: return projectionOf
    InstallationService ->>- scenario: return handleWorkOrder
    scenario ->>+ Repository: Transaction.commit
    Repository ->>+ Repository: INSERT installation_events
    Repository ->>- Repository: return INSERT installation_events
    Repository ->>+ Repository: INSERT installation
    Repository ->>- Repository: return INSERT installation
    Repository ->>- scenario: return Transaction.commit
    scenario ->>+ http: GET
    http ->>+ http: GET %FE%FF%00%2Finstallations
    http ->>+ http: GET
    http ->>- http: return GET
    http ->>+ http: GET
    http ->>- http: return GET
    http ->>+ Repository: test
    Repository ->>- http: return test
    http ->>+ InstallationReadModel: query
    InstallationReadModel ->>+ Repository: findAllMatching
    Repository ->>+ Repository: SELECT installation
    Repository ->>+ Repository: SELECT installation
    Repository ->>- Repository: return SELECT installation
    Repository ->>- Repository: return SELECT installation
    Repository ->>- InstallationReadModel: return findAllMatching
    InstallationReadModel ->>- http: return query
    http ->>+ Repository: Transaction.commit
    Repository ->>- http: return Transaction.commit
    http ->>- scenario: return GET
    http ->>- http: return GET %FE%FF%00%2Finstallations
    scenario ->>+ http: GET
    http ->>+ http: GET %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D
    http ->>+ InstallationReadModel: queryByOrderId
    InstallationReadModel ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>+ Repository: SELECT installation
    Repository ->>- Repository: return SELECT installation
    Repository ->>- Repository: return Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return findById
    InstallationReadModel ->>- http: return queryByOrderId
    http ->>+ Repository: Transaction.commit
    Repository ->>- http: return Transaction.commit
    http ->>- scenario: return GET
    http ->>- http: return GET %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D
    scenario ->>+ http: PATCH
    http ->>+ http: PATCH %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D
    http ->>+ InstallationService: assignDevice
    InstallationService ->>+ Repository: findByOrderId
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- InstallationService: return findByOrderId
    InstallationService ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>+ Repository: Transaction.commit
    Repository ->>+ Repository: INSERT installation_events
    Repository ->>- Repository: return INSERT installation_events
    Repository ->>- Repository: return Transaction.commit
    Repository ->>- InstallationService: return save
    InstallationService ->>+ KnownDevicesReadModel: projectionOfDeviceInstallation
    KnownDevicesReadModel ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.communication.KnownDevicesReadModel%FE%FF%00%24KnownDeviceEntity
    Repository ->>+ Repository: SELECT known_device
    Repository ->>- Repository: return SELECT known_device
    Repository ->>- Repository: return Session.find devices.configuration.communication.KnownDevicesReadModel%FE%FF%00%24KnownDeviceEntity
    Repository ->>- KnownDevicesReadModel: return findById
    KnownDevicesReadModel ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.communication.KnownDevicesReadModel%FE%FF%00%24KnownDeviceEntity
    Repository ->>+ Repository: SELECT known_device
    Repository ->>- Repository: return SELECT known_device
    Repository ->>- Repository: return Session.merge devices.configuration.communication.KnownDevicesReadModel%FE%FF%00%24KnownDeviceEntity
    Repository ->>- KnownDevicesReadModel: return save
    KnownDevicesReadModel ->>- InstallationService: return projectionOfDeviceInstallation
    InstallationService ->>+ Repository: Transaction.commit
    Repository ->>+ Repository: INSERT known_device
    Repository ->>- Repository: return INSERT known_device
    Repository ->>- InstallationService: return Transaction.commit
    InstallationService ->>+ InstallationReadModel: projectionOf
    InstallationReadModel ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>+ Repository: SELECT installation
    Repository ->>- Repository: return SELECT installation
    Repository ->>- Repository: return Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return findById
    InstallationReadModel ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return save
    InstallationReadModel ->>- InstallationService: return projectionOf
    InstallationService ->>+ Repository: Transaction.commit
    Repository ->>+ Repository: UPDATE installation
    Repository ->>- Repository: return UPDATE installation
    Repository ->>- InstallationService: return Transaction.commit
    InstallationService ->>- http: return assignDevice
    http ->>+ Repository: findByOrderId
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- http: return findByOrderId
    http ->>- scenario: return PATCH
    http ->>- http: return PATCH %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D
    scenario ->>+ http: PATCH
    http ->>+ http: PATCH %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D
    http ->>+ InstallationService: assignLocation
    InstallationService ->>+ Repository: findByOrderId
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- InstallationService: return findByOrderId
    InstallationService ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>+ Repository: Transaction.commit
    Repository ->>+ Repository: INSERT installation_events
    Repository ->>- Repository: return INSERT installation_events
    Repository ->>- Repository: return Transaction.commit
    Repository ->>- InstallationService: return save
    InstallationService ->>+ InstallationReadModel: projectionOf
    InstallationReadModel ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>+ Repository: SELECT installation
    Repository ->>- Repository: return SELECT installation
    Repository ->>- Repository: return Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return findById
    InstallationReadModel ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return save
    InstallationReadModel ->>- InstallationService: return projectionOf
    InstallationService ->>+ Repository: Transaction.commit
    Repository ->>- InstallationService: return Transaction.commit
    InstallationService ->>- http: return assignLocation
    http ->>+ Repository: findByOrderId
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- http: return findByOrderId
    http ->>- scenario: return PATCH
    http ->>- http: return PATCH %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D
    scenario ->>+ InstallationService: handleBootNotification
    InstallationService ->>+ Repository: findByDeviceId
    Repository ->>+ Repository: SELECT
    Repository ->>+ Repository: SELECT
    Repository ->>- Repository: return SELECT
    Repository ->>- Repository: return SELECT
    Repository ->>- InstallationService: return findByDeviceId
    InstallationService ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>- InstallationService: return save
    InstallationService ->>+ InstallationReadModel: projectionOf
    InstallationReadModel ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>+ Repository: SELECT installation
    Repository ->>- Repository: return SELECT installation
    Repository ->>- Repository: return Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return findById
    InstallationReadModel ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return save
    InstallationReadModel ->>- InstallationService: return projectionOf
    InstallationService ->>- scenario: return handleBootNotification
    scenario ->>+ Repository: Transaction.commit
    Repository ->>+ Repository: INSERT installation_events
    Repository ->>- Repository: return INSERT installation_events
    Repository ->>+ Repository: UPDATE installation
    Repository ->>- Repository: return UPDATE installation
    Repository ->>- scenario: return Transaction.commit
    scenario ->>+ http: GET
    http ->>+ http: GET %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D
    http ->>+ InstallationReadModel: queryByOrderId
    InstallationReadModel ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>+ Repository: SELECT installation
    Repository ->>- Repository: return SELECT installation
    Repository ->>- Repository: return Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return findById
    InstallationReadModel ->>- http: return queryByOrderId
    http ->>+ Repository: Transaction.commit
    Repository ->>- http: return Transaction.commit
    http ->>- scenario: return GET
    http ->>- http: return GET %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D
    scenario ->>+ http: PATCH
    http ->>+ http: PATCH %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D
    http ->>+ InstallationService: confirmBootData
    InstallationService ->>+ Repository: findByOrderId
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- InstallationService: return findByOrderId
    InstallationService ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>+ Repository: Transaction.commit
    Repository ->>+ Repository: INSERT installation_events
    Repository ->>- Repository: return INSERT installation_events
    Repository ->>- Repository: return Transaction.commit
    Repository ->>- InstallationService: return save
    InstallationService ->>+ InstallationReadModel: projectionOf
    InstallationReadModel ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>+ Repository: SELECT installation
    Repository ->>- Repository: return SELECT installation
    Repository ->>- Repository: return Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return findById
    InstallationReadModel ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return save
    InstallationReadModel ->>- InstallationService: return projectionOf
    InstallationService ->>+ Repository: Transaction.commit
    Repository ->>- InstallationService: return Transaction.commit
    InstallationService ->>- http: return confirmBootData
    http ->>+ Repository: findByOrderId
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- http: return findByOrderId
    http ->>- http: return PATCH %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D
    http ->>- scenario: return PATCH
    scenario ->>+ http: PATCH
    http ->>+ http: PATCH %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D
    http ->>+ InstallationService: complete
    InstallationService ->>+ Repository: findByOrderId
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- InstallationService: return findByOrderId
    InstallationService ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationEventSourcingRepository%FE%FF%00%24InstallationEventEntity
    Repository ->>+ Repository: Transaction.commit
    Repository ->>+ Repository: INSERT installation_events
    Repository ->>- Repository: return INSERT installation_events
    Repository ->>- Repository: return Transaction.commit
    Repository ->>- InstallationService: return save
    InstallationService ->>+ KnownDevicesReadModel: projectionOfInstallationCompleted
    KnownDevicesReadModel ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.communication.KnownDevicesReadModel%FE%FF%00%24KnownDeviceEntity
    Repository ->>+ Repository: SELECT known_device
    Repository ->>- Repository: return SELECT known_device
    Repository ->>- Repository: return Session.find devices.configuration.communication.KnownDevicesReadModel%FE%FF%00%24KnownDeviceEntity
    Repository ->>- KnownDevicesReadModel: return findById
    KnownDevicesReadModel ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.communication.KnownDevicesReadModel%FE%FF%00%24KnownDeviceEntity
    Repository ->>- Repository: return Session.merge devices.configuration.communication.KnownDevicesReadModel%FE%FF%00%24KnownDeviceEntity
    Repository ->>- KnownDevicesReadModel: return save
    KnownDevicesReadModel ->>- InstallationService: return projectionOfInstallationCompleted
    InstallationService ->>+ Repository: Transaction.commit
    Repository ->>+ Repository: UPDATE known_device
    Repository ->>- Repository: return UPDATE known_device
    Repository ->>- InstallationService: return Transaction.commit
    InstallationService ->>+ InstallationReadModel: projectionOf
    InstallationReadModel ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>+ Repository: SELECT installation
    Repository ->>- Repository: return SELECT installation
    Repository ->>- Repository: return Session.find devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return findById
    InstallationReadModel ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- Repository: return Session.merge devices.configuration.installations.InstallationReadModel%FE%FF%00%24InstallationEntity
    Repository ->>- InstallationReadModel: return save
    InstallationReadModel ->>- InstallationService: return projectionOf
    InstallationService ->>+ Repository: Transaction.commit
    Repository ->>+ Repository: UPDATE installation
    Repository ->>- Repository: return UPDATE installation
    Repository ->>- InstallationService: return Transaction.commit
    InstallationService ->>+ InstallationsToDevicesMediator: create
    InstallationsToDevicesMediator ->>+ DeviceService: createNewDevice
    DeviceService ->>+ Repository: save
    Repository ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.device.DeviceDocumentWithHistoryRepository%FE%FF%00%24DeviceDocumentEntity
    Repository ->>+ Repository: SELECT device_document
    Repository ->>- Repository: return SELECT device_document
    Repository ->>- Repository: return Session.find devices.configuration.device.DeviceDocumentWithHistoryRepository%FE%FF%00%24DeviceDocumentEntity
    Repository ->>- Repository: return findById
    Repository ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.device.DeviceDocumentWithHistoryRepository%FE%FF%00%24DeviceDocumentEntity
    Repository ->>+ Repository: SELECT device_document
    Repository ->>- Repository: return SELECT device_document
    Repository ->>- Repository: return Session.merge devices.configuration.device.DeviceDocumentWithHistoryRepository%FE%FF%00%24DeviceDocumentEntity
    Repository ->>- Repository: return save
    Repository ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.device.DeviceDocumentWithHistoryRepository%FE%FF%00%24DeviceEventEntity
    Repository ->>+ Repository: SELECT device_events
    Repository ->>- Repository: return SELECT device_events
    Repository ->>- Repository: return Session.merge devices.configuration.device.DeviceDocumentWithHistoryRepository%FE%FF%00%24DeviceEventEntity
    Repository ->>- Repository: return save
    Repository ->>+ Repository: save
    Repository ->>+ Repository: Session.merge devices.configuration.device.DeviceDocumentWithHistoryRepository%FE%FF%00%24DeviceEventEntity
    Repository ->>+ Repository: SELECT device_events
    Repository ->>- Repository: return SELECT device_events
    Repository ->>- Repository: return Session.merge devices.configuration.device.DeviceDocumentWithHistoryRepository%FE%FF%00%24DeviceEventEntity
    Repository ->>- Repository: return save
    Repository ->>+ KnownDevicesReadModel: projectionOfDeInstallation
    KnownDevicesReadModel ->>- Repository: return projectionOfDeInstallation
    Repository ->>+ DevicesReadModel: projectionOf
    DevicesReadModel ->>+ Repository: findById
    Repository ->>+ Repository: Session.find devices.configuration.search.DevicesReadModel%FE%FF%00%24DeviceReadsEntity
    Repository ->>+ Repository: SELECT search
    Repository ->>- Repository: return SELECT search
    Repository ->>- Repository: return Session.find devices.configuration.search.DevicesReadModel%FE%FF%00%24DeviceReadsEntity
    Repository ->>- DevicesReadModel: return findById
    DevicesReadModel ->>+ Repository: save
    Repository ->>+ Repository: Session.persist devices.configuration.search.DevicesReadModel%FE%FF%00%24DeviceReadsEntity
    Repository ->>- Repository: return Session.persist devices.configuration.search.DevicesReadModel%FE%FF%00%24DeviceReadsEntity
    Repository ->>- DevicesReadModel: return save
    DevicesReadModel ->>- Repository: return projectionOf
    Repository ->>- DeviceService: return save
    DeviceService ->>- InstallationsToDevicesMediator: return createNewDevice
    InstallationsToDevicesMediator ->>+ Repository: Transaction.commit
    Repository ->>+ Repository: INSERT device_document
    Repository ->>- Repository: return INSERT device_document
    Repository ->>+ Repository: INSERT device_events
    Repository ->>- Repository: return INSERT device_events
    Repository ->>+ Repository: INSERT device_events
    Repository ->>- Repository: return INSERT device_events
    Repository ->>+ Repository: INSERT search
    Repository ->>- Repository: return INSERT search
    Repository ->>- InstallationsToDevicesMediator: return Transaction.commit
    InstallationsToDevicesMediator ->>- InstallationService: return create
    InstallationService ->>- http: return complete
    http ->>+ Repository: findByOrderId
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>+ Repository: SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- Repository: return SELECT installation_events
    Repository ->>- http: return findByOrderId
    http ->>- scenario: return PATCH
    http ->>- http: return PATCH %FE%FF%00%2Finstallations%FE%FF%00%2F%00%7BorderId%FE%FF%00%7D

```
