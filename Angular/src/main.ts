import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config'; // rutas-navegación entre componentes
import { AppComponent } from './app/app.component';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
