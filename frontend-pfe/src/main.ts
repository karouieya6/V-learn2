import '@angular/localize/init';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { provideRouter } from '@angular/router';
import { importProvidersFrom } from '@angular/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

bootstrapApplication(AppComponent, {
  providers: [provideHttpClient(),
    provideRouter(routes)
  ]
});
