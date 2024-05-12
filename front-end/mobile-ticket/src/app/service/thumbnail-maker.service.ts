import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ThumbnailMakerService {
  constructor() {}
  public getThumbnail(
    backgroundColor: string,
    fabricDecoration: any,
    thumbnailUrl: string
  ) {
    let ticketSVG = `<svg width="117" height="200" viewBox="0 0 117 200" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path fill-rule="evenodd" clip-rule="evenodd"
        d="M0 10C0 7.4375 0.96875 5.09375 2.55469 3.32031C4.38281 1.28125 7.03906 0 10 0H106.664C112.188 0 116.664 4.47656 116.664 10V140.008C111.297 140.188 107 144.586 107 150C107 155.414 111.297 159.812 116.664 159.992V190C116.664 195.523 112.188 200 106.664 200H10C4.47656 200 0 195.523 0 190V160C5.52344 160 10 155.523 10 150C10 144.477 5.52344 140 0 140V10Z"
        fill="${backgroundColor}" />
</svg>`;
    return ticketSVG;
  }
}
