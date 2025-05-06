import { Component, OnInit } from '@angular/core';
import Swiper from 'swiper';

import SwiperCore from 'swiper';
import { Autoplay, EffectFade } from 'swiper/modules';


SwiperCore.use([Autoplay, EffectFade]);

import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
@Component({
   standalone: true,
  selector: 'app-home-three-testimonial',
  templateUrl: './home-three-testimonial.component.html',
  styleUrls: ['./home-three-testimonial.component.scss'],
  imports: [
   CommonModule,
   RouterModule
 ],
})
export class HomeThreeTestimonialComponent implements OnInit {

   sliderData = [
     {
       id: 1,
       img: 'assets/img/testimonial/home-3/testi-1.jpg',
       text: '“After I started learning design with Quillow, I realized that I had improved to very advanced levels. While I am studying at my university, I design as an additional income and I am sure that I will do this professionally.”',
       name: 'James Lee',
       role: 'Student @V-Learn University'
     },
     {
       id: 2,
       img: 'assets/img/testimonial/home-3/testi-2.jpg',
       text: '“This platform helped me sharpen my frontend skills. The tutorials are spot-on and really easy to follow.”',
       name: 'Sara Kim',
       role: 'Frontend Developer'
     },
     {
       id: 3,
       img: 'assets/img/testimonial/home-3/testi-3.jpg',
       text: '“A great learning experience. I recommend it to anyone wanting to grow professionally.”',
       name: 'Ali Youssef',
       role: 'Engineer @ABC Corp'
     }
   ];
 
   constructor() {}
 
   ngOnInit(): void {}
 }
 