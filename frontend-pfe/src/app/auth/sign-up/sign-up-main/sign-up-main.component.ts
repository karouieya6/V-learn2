import { Component, OnInit } from '@angular/core';
import { HeaderThreeComponent} from '../../../pages/Home/header-three/header-three.component'; // adjust if needed
import { FooterComponent } from '../../../common/footer/footer.component'; // adjust path as needed
import { SignUpAreaComponent } from '../sign-up-area/sign-up-area.component'; // adjust path if needed
@Component({
  selector: 'app-sign-up-main',
  standalone: true,
  templateUrl: './sign-up-main.component.html',
  styleUrls: ['./sign-up-main.component.scss'],
  imports: [HeaderThreeComponent, FooterComponent, SignUpAreaComponent]
})
export class SignUpMainComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
