import { Component } from '@angular/core';
import { RouterModule } from '@angular/router'; //con esto, la barra puede manejar enlaces

@Component({
  selector: 'app-navbar',
  imports: [RouterModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {

}
