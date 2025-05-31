import { Routes } from '@angular/router';
import { AuthComponent } from './auth/auth.component';
import { ProfileComponent } from './profile/profile.component';
import { JobsComponent } from './jobs/jobs.component';
import { ClientsComponent } from './clients/clients.component';
import { CarsComponent } from './cars/cars.component';

export const routes: Routes = [
  { path: '', redirectTo: 'profile', pathMatch: 'full' },
  //{ path: 'auth', component: AuthComponent }, //está comentada pq aún no está implementada con supabase
  { path: 'profile', component: ProfileComponent },
  { path: 'jobs', component: JobsComponent },
  { path: 'clients', component: ClientsComponent },
  { path: 'cars', component: CarsComponent }
];

