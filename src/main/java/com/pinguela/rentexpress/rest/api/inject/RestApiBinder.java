package com.pinguela.rentexpress.rest.api.inject;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import com.pinguela.rentexpress.rest.api.service.GroqService;
import com.pinguela.rentexpress.rest.api.service.WeatherService;
import com.pinguela.rentexpress.rest.api.adapter.GroqApiAdapter;
import com.pinguela.rentexpress.rest.api.adapter.WeatherApiAdapter;
import com.pinguela.rentexpress.rest.api.adapter.impl.GroqApiAdapterImpl;
import com.pinguela.rentexpress.rest.api.adapter.impl.WeatherApiAdapterImpl;
import com.pinguela.rentexpres.service.OwnershipService;
import com.pinguela.rentexpres.service.impl.OwnershipServiceImpl;
import com.pinguela.rentexpress.rest.api.security.RateLimitStore;
import com.pinguela.rentexpress.rest.api.security.InMemoryRateLimitStore;
import com.pinguela.rentexpress.rest.api.security.RateLimitFilter;
import com.pinguela.rentexpres.service.AddressService;
import com.pinguela.rentexpres.service.CityService;
import com.pinguela.rentexpres.service.CloudinaryService;
import com.pinguela.rentexpres.service.ConversationService;
import com.pinguela.rentexpres.service.EmployeeService;
import com.pinguela.rentexpres.service.FileService;
import com.pinguela.rentexpres.service.HeadquartersService;
import com.pinguela.rentexpres.service.ImageService;
import com.pinguela.rentexpres.service.MailService;
import com.pinguela.rentexpres.service.MessageService;
import com.pinguela.rentexpres.service.ProvinceService;
import com.pinguela.rentexpres.service.RentalService;
import com.pinguela.rentexpres.service.RentalStatusService;
import com.pinguela.rentexpres.service.ReservationService;
import com.pinguela.rentexpres.service.ReservationStatusService;
import com.pinguela.rentexpres.service.RoleService;
import com.pinguela.rentexpres.service.StatisticsService;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpres.service.VehicleCategoryService;
import com.pinguela.rentexpres.service.VehicleService;
import com.pinguela.rentexpres.service.VehicleStatusService;
import com.pinguela.rentexpres.service.impl.AddressServiceImpl;
import com.pinguela.rentexpres.service.impl.CityServiceImpl;
import com.pinguela.rentexpres.service.impl.CloudinaryServiceImpl;
import com.pinguela.rentexpres.service.impl.ConversationServiceImpl;
import com.pinguela.rentexpres.service.impl.EmployeeServiceImpl;
import com.pinguela.rentexpres.service.impl.FileServiceImpl;
import com.pinguela.rentexpres.service.impl.HeadquartersServiceImpl;
import com.pinguela.rentexpres.service.impl.ImageServiceImpl;
import com.pinguela.rentexpres.service.impl.MailServiceImpl;
import com.pinguela.rentexpres.service.impl.MessageServiceImpl;
import com.pinguela.rentexpres.service.impl.ProvinceServiceImpl;
import com.pinguela.rentexpres.service.impl.RentalServiceImpl;
import com.pinguela.rentexpres.service.impl.RentalStatusServiceImpl;
import com.pinguela.rentexpres.service.impl.ReservationServiceImpl;
import com.pinguela.rentexpres.service.impl.ReservationStatusServiceImpl;
import com.pinguela.rentexpres.service.impl.RoleServiceImpl;
import com.pinguela.rentexpres.service.impl.StatisticsServiceImpl;
import com.pinguela.rentexpres.service.impl.UserServiceImpl;
import com.pinguela.rentexpres.service.impl.VehicleCategoryServiceImpl;
import com.pinguela.rentexpres.service.impl.VehicleServiceImpl;
import com.pinguela.rentexpres.service.impl.VehicleStatusServiceImpl;

/**
 * Binder HK2/Jersey para inyección de dependencias.
 * Registra interfaces de servicio (middleware y API) con sus implementaciones.
 */
public final class RestApiBinder extends AbstractBinder {
 
    @Override
    protected void configure() {
        // API REST (servicios propios)
        bind(GroqApiAdapterImpl.class).to(GroqApiAdapter.class);
        bind(GroqService.class).to(GroqService.class);
        bind(WeatherApiAdapterImpl.class).to(WeatherApiAdapter.class);
        bind(WeatherService.class).to(WeatherService.class);
        bind(OwnershipServiceImpl.class).to(OwnershipService.class);

        // Rate limit:
        bind(InMemoryRateLimitStore.class).to(RateLimitStore.class);
        bind(RateLimitFilter.class);

        // Middleware: servicios de dominio
        bind(UserServiceImpl.class).to(UserService.class);
        bind(EmployeeServiceImpl.class).to(EmployeeService.class);
        bind(RoleServiceImpl.class).to(RoleService.class);
        bind(CloudinaryServiceImpl.class).to(CloudinaryService.class);
        bind(AddressServiceImpl.class).to(AddressService.class);
        bind(CityServiceImpl.class).to(CityService.class);
        bind(ProvinceServiceImpl.class).to(ProvinceService.class);
        bind(ReservationServiceImpl.class).to(ReservationService.class);
        bind(ReservationStatusServiceImpl.class).to(ReservationStatusService.class);
        bind(RentalServiceImpl.class).to(RentalService.class);
        bind(RentalStatusServiceImpl.class).to(RentalStatusService.class);
        bind(VehicleServiceImpl.class).to(VehicleService.class);
        bind(VehicleCategoryServiceImpl.class).to(VehicleCategoryService.class);
        bind(VehicleStatusServiceImpl.class).to(VehicleStatusService.class);
        bind(ImageServiceImpl.class).to(ImageService.class);
        bind(FileServiceImpl.class).to(FileService.class);
        bind(HeadquartersServiceImpl.class).to(HeadquartersService.class);
        bind(ConversationServiceImpl.class).to(ConversationService.class);
        bind(MessageServiceImpl.class).to(MessageService.class);
        bind(StatisticsServiceImpl.class).to(StatisticsService.class);
        bind(MailServiceImpl.class).to(MailService.class);
    }
}
