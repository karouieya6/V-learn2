import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HeaderThreeComponent} from '../../../pages/Home/header-three/header-three.component'; // adjust if needed
import { FooterComponent } from '../../../common/footer/footer.component'; // adjust path as needed
import { SignInAreaComponent } from '../sign-in-area/sign-in-area.component'; // adjust path if needed
@Component({
  selector: 'app-sign-in-main',
  standalone: true,
  templateUrl: './sign-in-main.component.html',
  styleUrls: ['./sign-in-main.component.scss'],
  imports: [
    RouterModule, HeaderThreeComponent, FooterComponent, SignInAreaComponent
  ],
})
export class SignInMainComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
