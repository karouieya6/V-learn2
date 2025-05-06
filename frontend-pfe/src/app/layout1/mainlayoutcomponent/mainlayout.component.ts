import { Component} from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderThreeComponent } from '../../pages/Home/header-three/header-three.component';
import { FooterComponent } from '../../common/footer/footer.component'
import { RouterModule } from '@angular/router';
@Component({
    selector: 'app-main-layout',
    standalone: true,
    imports: [HeaderThreeComponent, FooterComponent, RouterModule,CommonModule],
    templateUrl: './mainlayout.component.html'
  })
  export class MainLayoutComponent {}
  