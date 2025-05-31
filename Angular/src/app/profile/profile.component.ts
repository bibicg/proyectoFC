import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; //imprescindible al usar standalone components

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],//imprescindible la doble importación, sino no se va a ver!!!
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent {

}
